package com.loopmarket.domain.product.service;

import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.product.dto.CategoryProductStatsDTO;
import com.loopmarket.domain.product.dto.ProductDTO;
import com.loopmarket.domain.product.dto.WeeklyProductStatsDTO;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {

	private final ProductRepository productRepository; // 상품 데이터베이스 작업용 리포지토리
	private final ImageService imageService; // 이미지 저장 및 조회 서비스
	private final CategoryRepository categoryRepository; // 카테고리 정보 조회용 리포지토리
  private final WishlistRepository wishlistRepository;

  /**
	 * 상품 정보와 이미지들을 함께 저장하는 메서드
	 *
	 * @param product        저장할 상품 엔티티
	 * @param images         업로드된 이미지 파일 리스트
	 * @param mainImageIndex 대표 이미지로 설정할 이미지 인덱스
	 */
	public void registerProductWithImages(ProductEntity product, List<MultipartFile> images, int mainImageIndex) {
		product.setCreatedAt(LocalDateTime.now()); // 상품 등록일 현재 시간 설정
		product.setUpdateAt(LocalDateTime.now()); // 상품 수정일 현재 시간 설정
		product.setStatus("ONSALE"); // 기본 상태 'ONSALE'으로 설정
		//product.setIsHidden(true); // 상품 숨김 여부 true로 설정 (필요 시 수정)

		// 1. 상품 엔티티 먼저 저장
		ProductEntity savedProduct = productRepository.save(product);

		// 2. 이미지가 있으면 상품 ID와 함께 이미지 저장 처리
		if (images != null && !images.isEmpty()) {
			imageService.saveImagesForProduct(savedProduct.getProductId(), images, mainImageIndex);
		}
	}

	/**
	 * 모든 상품을 조회하고, 각 상품에 대표 이미지와 전체 이미지 경로를 세팅해 반환
	 *
	 * @return 대표 이미지와 이미지 리스트가 포함된 상품 리스트
	 */
  public List<ProductEntity> getAllProducts() {
    List<ProductEntity> products = productRepository.findAll();

    for (ProductEntity product : products) {
      Long productId = product.getProductId();

      // 대표 이미지 경로 조회 후 상품 엔티티에 세팅
      String thumbnailPath = imageService.getThumbnailPath(productId);
      product.setThumbnailPath(thumbnailPath);

      // 전체 이미지 경로 리스트 조회 후 상품 엔티티에 세팅 (null 방지)
      List<String> imagePaths = imageService.getAllImagePaths(productId);
      product.setImagePaths(imagePaths != null ? imagePaths : List.of());

      // 인기 상품 조건 세팅: 찜 5 이상 or 조회수 30 이상
      long wishCount = wishlistRepository.countByProdId(productId);
      Integer viewCount = product.getViewCount() != null ? product.getViewCount() : 0;

      product.setIsBest(wishCount >= 5 || viewCount >= 30);
    }
    return products;
  }


  // 결제 페이지에 필요하여 추가했습니다 - jw
	public ProductEntity getProductById(Long id) {
		return productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
	}

	// 결제 관련 상품 상태 변경을 위해 추가했습니다 - jw
	public void updateProductStatus(Long productId, String newStatus) {
		ProductEntity product = getProductById(productId); // 기존 메서드 재사용
		product.setStatus(newStatus);
		product.setUpdateAt(LocalDateTime.now()); // 상태 변경 시 수정일도 갱신
		productRepository.save(product);
	}

	// 판매 중인 상품만 조회하는 메서드 추가했습니다 - jw
	public List<ProductEntity> getMyProducts(Long sellerId) {
		return productRepository.findByUserIdAndStatus(sellerId, "ONSALE");
	}

	// 판매 중(ONSALE) + 예약중(RESERVED) 상태 상품 조회 - QR 생성, 판매중 탭 용 - jw
	public List<ProductEntity> getOngoingProducts(Long sellerId) {
		List<String> statuses = List.of("ONSALE", "RESERVED");
		List<ProductEntity> products = productRepository.findByUserIdAndStatusIn(sellerId, statuses);

		for (ProductEntity product : products) {
			Long productId = product.getProductId();
			product.setThumbnailPath(imageService.getThumbnailPath(productId));
			product.setImagePaths(imageService.getAllImagePaths(productId));
		}
		return products;
	}

	// 판매 중(ONSALE) + 예약중(RESERVED) 상태 상품 조회 - QR 생성, 판매중 탭 용 - jw
	public List<ProductEntity> getVisibleOngoingProducts(Long sellerId) {
		List<String> statuses = List.of("ONSALE", "RESERVED");
		List<ProductEntity> products = productRepository.findByUserIdAndStatusInAndIsHiddenFalse(sellerId, statuses);

		for (ProductEntity product : products) {
			Long productId = product.getProductId();
			product.setThumbnailPath(imageService.getThumbnailPath(productId));
			product.setImagePaths(imageService.getAllImagePaths(productId));
		}
		return products;
	}

	// 대표 이미지 경로를 받아오는 메서드 추가했습니다 - jw
	public String getThumbnailPath(Long productId) {
		return imageService.getThumbnailPath(productId);
	}

	// 이미지 가져올려고 썼습니다 (이미지 없을시 경로 변경했습니다-kw)
	public String getProfileImagePath(String profileImgId) {
		if (profileImgId == null || profileImgId.trim().isEmpty()) {
			return "/img/default-profile.png";
		}
		return "/images/profiles/" + profileImgId;
	}

	// 판매완료 정보 가져오기
	public List<ProductEntity> getSoldProductsWithThumbnail(Long userId) {
		List<ProductEntity> soldProducts = productRepository.findByUserIdAndStatus(userId, "SOLD");

		for (ProductEntity product : soldProducts) {
			String thumbnailPath = imageService.getThumbnailPath(product.getProductId());
			product.setThumbnailPath(thumbnailPath);
		}

		return soldProducts;
	}

  @Transactional
  public void incrementViewCount(Long productId) {
    ProductEntity product = productRepository.findById(productId)
      .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

    int currentCount = product.getViewCount() != null ? product.getViewCount() : 0;
    product.setViewCount(currentCount + 1);
  }



  public List<ProductEntity> getNearbyProductsByCategory(Integer ctgCode, double lat, double lng, int min, int max) {
    List<ProductEntity> allProducts = productRepository.findByIsHiddenFalseAndStatusAndCtgCode("ONSALE", ctgCode);

    double radiusKm = 25.0;
    List<ProductEntity> nearby = allProducts.stream()
      .filter(product -> {
        Double productLat = product.getLatitude();
        Double productLng = product.getLongitude();
        if (productLat == null || productLng == null) return false;

        double distance = calculateDistanceKm(lat, lng, productLat, productLng);
        return distance <= radiusKm
          && product.getPrice() != null
          && product.getPrice() >= min
          && product.getPrice() <= max;
      })
      .collect(Collectors.toList());

    for (ProductEntity product : nearby) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }

    return nearby;
  }

  public List<ProductEntity> getNearbyProductsByMainCategory(Integer mainCategoryCode, double lat, double lng, int min, int max) {
    List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
    List<ProductEntity> allProducts = productRepository.findByIsHiddenFalseAndStatusAndCtgCodeIn("ONSALE", subCategoryCodes);

    double radiusKm = 25.0;
    List<ProductEntity> nearby = allProducts.stream()
      .filter(product -> {
        Double productLat = product.getLatitude();
        Double productLng = product.getLongitude();
        if (productLat == null || productLng == null) return false;

        double distance = calculateDistanceKm(lat, lng, productLat, productLng);
        return distance <= radiusKm
          && product.getPrice() != null
          && product.getPrice() >= min
          && product.getPrice() <= max;
      })
      .collect(Collectors.toList());

    for (ProductEntity product : nearby) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }

    return nearby;
  }


//	public List<ProductEntity> getProductsByMainCategory(Integer mainCategoryCode) {
//		List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
//		List<ProductEntity> products = productRepository.findByCtgCodeIn(subCategoryCodes);
//
//		for (ProductEntity product : products) {
//			Long productId = product.getProductId();
//			product.setThumbnailPath(imageService.getThumbnailPath(productId));
//			product.setImagePaths(imageService.getAllImagePaths(productId));
//		}
//
//		return products;
//	}

	// 전체 가격 필터
	public List<ProductEntity> getProductsByPriceRange(Integer min, Integer max) {
    List<ProductEntity> products = productRepository.findByIsHiddenFalseAndPriceBetweenOrderByCreatedAtDesc(min, max);
	    for (ProductEntity product : products) {
	        Long productId = product.getProductId();
	        product.setThumbnailPath(imageService.getThumbnailPath(productId));
	        product.setImagePaths(imageService.getAllImagePaths(productId));
	    }
	    return products;
	}

//	public List<ProductEntity> getProductsByPriceRange(Integer min, Integer max) {
//		List<ProductEntity> products = productRepository.findByPriceBetween(min, max);
//		for (ProductEntity product : products) {
//			Long productId = product.getProductId();
//			product.setThumbnailPath(imageService.getThumbnailPath(productId));
//			product.setImagePaths(imageService.getAllImagePaths(productId));
//		}
//		return products;
//	}

	// 대분류 + 가격 필터
	public List<ProductEntity> getProductsByMainCategoryAndPrice(Integer mainCategoryCode, Integer min, Integer max) {
	    List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
	    List<ProductEntity> products = productRepository.findByIsHiddenFalseAndCtgCodeInAndPriceBetween(subCategoryCodes, min, max);
	    for (ProductEntity product : products) {
	        Long productId = product.getProductId();
	        product.setThumbnailPath(imageService.getThumbnailPath(productId));
	        product.setImagePaths(imageService.getAllImagePaths(productId));
	    }
	    return products;
	}

//	public List<ProductEntity> getProductsByMainCategoryAndPrice(Integer mainCategoryCode, Integer min, Integer max) {
//		List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
//		List<ProductEntity> products = productRepository.findByCtgCodeInAndPriceBetween(subCategoryCodes, min, max);
//		for (ProductEntity product : products) {
//			Long productId = product.getProductId();
//			product.setThumbnailPath(imageService.getThumbnailPath(productId));
//			product.setImagePaths(imageService.getAllImagePaths(productId));
//		}
//		return products;
//	}

	// 소분류 + 가격 필터
	public List<ProductEntity> getProductsByCategoryAndPrice(Integer ctgCode, Integer min, Integer max) {
		List<ProductEntity> products = productRepository.findByCtgCodeAndPriceBetweenAndIsHiddenFalse(ctgCode, min, max);
		for (ProductEntity product : products) {
			Long productId = product.getProductId();
			product.setThumbnailPath(imageService.getThumbnailPath(productId));
			product.setImagePaths(imageService.getAllImagePaths(productId));
		}
		return products;
		//return productRepository.findByCtgCodeAndPriceBetweenAndIsHiddenFalse(ctgCode, min, max);

	}

	// ID 기반 상세 조회 시 이미지 경로 세팅 포함
	public ProductEntity findById(Long id) {
		ProductEntity product = productRepository.findById(id)
				.orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다: " + id));

		product.setImagePaths(imageService.getAllImagePaths(id));
		product.setThumbnailPath(imageService.getThumbnailPath(id));

		return product;
	}

	// 검색어 기반 상품 조회
	public List<ProductEntity> searchProductsByKeyword(String keyword) {
	    List<ProductEntity> products = productRepository
	            .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

	    for (ProductEntity product : products) {
	        Long productId = product.getProductId();
	        product.setThumbnailPath(imageService.getThumbnailPath(productId));
	        product.setImagePaths(imageService.getAllImagePaths(productId));
	    }

	    return products;
	}

	// 상품 등록 수 통계용 - 최근 4주 주차별 통계
	public List<WeeklyProductStatsDTO> getWeeklyProductStats() {
	    List<Object[]> result = productRepository.countProductByWeekLastMonth();
	    return result.stream()
	            .map((Object[] row) -> new WeeklyProductStatsDTO((String) row[0], ((Number) row[1]).intValue()))
	            .collect(Collectors.toList());
	}

	// 상품 등록 수 통계용 - 최근 한 달 전체 상품 등록 수
	public int countTotalProductsLastMonth() {
	    LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
	    return productRepository.countByCreatedAtAfter(oneMonthAgo);
	}

	// 카테고리별 상품 등록 통계
	public List<CategoryProductStatsDTO> getCategoryProductStats() {
	    List<Object[]> raw = productRepository.countProductsByCategory();
	    return raw.stream()
	            .map(row -> new CategoryProductStatsDTO(
	                    ((Number) row[0]).intValue(),      // categoryCode
	                    (String) row[1],                   // categoryName
	                    ((Number) row[2]).intValue()))    // count
	            .collect(Collectors.toList());
	}
	
	// 카테고리별 상품 등록 통계
	public List<CategoryProductStatsDTO> getTopCategoryProductStats() {
	    List<Object[]> raw = productRepository.countProductsByTopCategory();
	    return raw.stream()
	            .map(row -> new CategoryProductStatsDTO(
	                    ((Number) row[0]).intValue(),      // categoryCode
	                    (String) row[1],                   // categoryName
	                    ((Number) row[2]).intValue()))    // count
	            .collect(Collectors.toList());
	}

  @Transactional
  public void updateProductWithImages(Long id, ProductEntity updatedProduct, List<MultipartFile> images, int mainImageIndex) {
    ProductEntity existing = productRepository.findById(id)
      .orElseThrow(() -> new NoSuchElementException("해당 상품이 존재하지 않습니다: " + id));

    existing.setTitle(updatedProduct.getTitle());
    existing.setPrice(updatedProduct.getPrice());
    existing.setSaleType(updatedProduct.getSaleType());
    existing.setCtgCode(updatedProduct.getCtgCode());
    existing.setCondition(updatedProduct.getCondition());
    existing.setConditionScore(updatedProduct.getConditionScore());
    existing.setDescription(updatedProduct.getDescription());
    existing.setIsDirect(updatedProduct.getIsDirect());
    existing.setIsDelivery(updatedProduct.getIsDelivery());
    existing.setIsNonface(updatedProduct.getIsNonface());
    existing.setLocationText(updatedProduct.getLocationText());
    existing.setLatitude(updatedProduct.getLatitude());
    existing.setLongitude(updatedProduct.getLongitude());
    existing.setShippingFee(updatedProduct.getShippingFee());
    existing.setUpdateAt(LocalDateTime.now());

/*    if (images != null && !images.isEmpty()) {
      imageService.replaceImagesForProduct(id, images, mainImageIndex);
    }*/

    productRepository.save(existing);
  }


  @Transactional
	public void updateHiddenStatus(Long productId, boolean hidden) {
	    ProductEntity product = productRepository.findById(productId)
	        .orElseThrow(() -> new RuntimeException("상품 없음"));
	    product.setIsHidden(hidden);
	}

	@Transactional
	public void updateCategory(Long productId, Integer newCode) {
	    ProductEntity product = productRepository.findById(productId)
	        .orElseThrow(() -> new RuntimeException("상품 없음"));
	    product.setCtgCode(newCode);
	}

	@Transactional
	public void deleteProductById(Long productId) {
	    productRepository.deleteById(productId);
	}

	public Page<ProductEntity> getProductsPage(Pageable pageable) {
	    return productRepository.findAll(pageable);
	}

  public List<ProductEntity> getNearbyProducts(double lat, double lng) {
    List<ProductEntity> allProducts = productRepository.findByIsHiddenFalseAndStatus("ONSALE");

    double radiusKm = 25.0;
    List<ProductEntity> nearby = allProducts.stream()
      .filter(product -> {
        Double productLat = product.getLatitude();
        Double productLng = product.getLongitude();
        if (productLat == null || productLng == null) return false;

        double distance = calculateDistanceKm(lat, lng, productLat, productLng);
        return distance <= radiusKm;
      })
      .collect(Collectors.toList());

    for (ProductEntity product : nearby) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }

    return nearby;
  }

  private double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
    double earthRadius = 6371.0; // 지구 반지름 (단위: km)

    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
      + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
      * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c;
  }

  public List<ProductEntity> getOtherProductsBySeller(Long sellerId, Long excludeProductId) {
    List<ProductEntity> products = productRepository
      .findByUserIdAndProductIdNotOrderByCreatedAtDesc(sellerId, excludeProductId);

    // 최대 8개까지만 보여줌
    products = products.stream().limit(8).collect(Collectors.toList());

    for (ProductEntity p : products) {
      Long productId = p.getProductId();
      p.setThumbnailPath(imageService.getThumbnailPath(productId));
      if (p.getCreatedAt() != null) {
        p.setRelativeTime(formatRelativeTimeInternal(p.getCreatedAt()));
      }
    }

    return products;
  }

  public List<ProductEntity> getSimilarProducts(Long excludeId, Integer ctgCode) {
    List<ProductEntity> list = productRepository.findByCtgCodeAndProductIdNotOrderByCreatedAtDesc(ctgCode, excludeId);

    for (ProductEntity p : list) {
      Long productId = p.getProductId();
      p.setThumbnailPath(imageService.getThumbnailPath(productId));
      p.setImagePaths(imageService.getAllImagePaths(productId));
      p.setRelativeTime(formatRelativeTimeInternal(p.getCreatedAt()));
    }

    // 최대 8개까지
    return list.stream().limit(8).collect(Collectors.toList());
  }


  private String formatRelativeTimeInternal(LocalDateTime createdAt) {
    Duration duration = Duration.between(createdAt, LocalDateTime.now());
    long hours = duration.toHours();
    long days = duration.toDays();
    if (hours < 1) return "방금 전";
    else if (hours < 24) return hours + "시간 전";
    else if (days < 2) return "1일 전";
    else return days + "일 전";
  }
  public void registerProductWithDTO(ProductDTO dto, Long userId, List<MultipartFile> images, int mainImageIndex) {
    ProductEntity entity = new ProductEntity();

    entity.setUserId(userId);

    entity.setTitle(dto.getTitle());
    entity.setPrice(dto.getPrice());
    entity.setCtgCode(dto.getCtgCode());
    entity.setDescription(dto.getDescription());
    entity.setSaleType(dto.getSaleType());
    entity.setCondition(dto.getCondition());
    entity.setConditionScore(dto.getConditionScore());
    entity.setIsDirect(dto.getIsDirect());
    entity.setIsDelivery(dto.getIsDelivery());
    entity.setIsNonface(dto.getIsNonface());
    entity.setLocationText(dto.getLocationText());
    entity.setLatitude(dto.getLatitude());
    entity.setLongitude(dto.getLongitude());
    entity.setShippingFee(dto.getShippingFee());
    entity.setIsHidden(false);

    registerProductWithImages(entity, images, mainImageIndex);
  }
  
  
  public Optional<ProductEntity> findProductById(Long productId) {
	    return productRepository.findById(productId);
	}


}
