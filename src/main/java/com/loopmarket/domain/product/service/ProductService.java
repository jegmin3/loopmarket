package com.loopmarket.domain.product.service;

import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final ProductRepository productRepository; // 상품 데이터베이스 작업용 리포지토리
  private final ImageService imageService;           // 이미지 저장 및 조회 서비스
  private final CategoryRepository categoryRepository;

  /**
   * 상품 정보와 이미지들을 함께 저장하는 메서드
   *
   * @param product 저장할 상품 엔티티
   * @param images 업로드된 이미지 파일 리스트
   * @param mainImageIndex 대표 이미지로 설정할 이미지 인덱스
   */
  public void registerProductWithImages(ProductEntity product, List<MultipartFile> images, int mainImageIndex) {
    product.setCreatedAt(LocalDateTime.now());  // 상품 등록일 현재 시간 설정
    product.setUpdateAt(LocalDateTime.now());   // 상품 수정일 현재 시간 설정
    product.setStatus("ONSALE");                 // 기본 상태 'ONSALE'으로 설정
    product.setIsHidden(true);                   // 상품 숨김 여부 true로 설정 (필요 시 수정)

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

      // 전체 이미지 경로 리스트 조회 후 상품 엔티티에 세팅
      List<String> imagePaths = imageService.getAllImagePaths(productId);
      product.setImagePaths(imagePaths != null ? imagePaths : List.of()); // null 방지
    }

    return products;
  }

  //결제 페이지에 필요하여 추가했습니다
  public ProductEntity getProductById(Long id) {
	    return productRepository.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
	}

  // 이미지 가져올려고 썼습니다
  public String getProfileImagePath(String profileImgId) {
		if (profileImgId == null || profileImgId.trim().isEmpty()) {
		    return "/images/default-profile.png";
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

  public List<ProductEntity> getProductsByCategory(Integer ctgCode) {
    List<ProductEntity> products = productRepository.findByCtgCode(ctgCode);

    for (ProductEntity product : products) {
      Long productId = product.getProductId();

      // 대표 이미지 경로 세팅
      String thumbnailPath = imageService.getThumbnailPath(productId);
      product.setThumbnailPath(thumbnailPath);

      // 전체 이미지 경로 리스트 세팅
      List<String> imagePaths = imageService.getAllImagePaths(productId);
      product.setImagePaths(imagePaths);
    }

    return products;
  }

  public List<ProductEntity> getProductsByMainCategory(Integer mainCategoryCode) {
    // 1. 해당 대분류의 하위 소분류 코드들 가져오기
    List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);

    // 2. 해당 소분류 코드로 상품 가져오기
    List<ProductEntity> products = productRepository.findByCtgCodeIn(subCategoryCodes);

    // 3. 이미지 정보 설정 (기존 방식 그대로)
    for (ProductEntity product : products) {
      Long productId = product.getProductId();
      String thumbnailPath = imageService.getThumbnailPath(productId);
      product.setThumbnailPath(thumbnailPath);

      List<String> imagePaths = imageService.getAllImagePaths(productId);
      product.setImagePaths(imagePaths != null ? imagePaths : List.of());
    }

    return products;
  }

  // 전체 가격 필터
  public List<ProductEntity> getProductsByPriceRange(Integer min, Integer max) {
    List<ProductEntity> products = productRepository.findByPriceBetween(min, max);
    for (ProductEntity product : products) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }
    return products;
  }

  // 대분류 + 가격 필터
  public List<ProductEntity> getProductsByMainCategoryAndPrice(Integer mainCategoryCode, Integer min, Integer max) {
    List<Integer> subCategoryCodes = categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
    List<ProductEntity> products = productRepository.findByCtgCodeInAndPriceBetween(subCategoryCodes, min, max);
    for (ProductEntity product : products) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }
    return products;
  }

  // 소분류 + 가격 필터
  public List<ProductEntity> getProductsByCategoryAndPrice(Integer ctgCode, Integer min, Integer max) {
    List<ProductEntity> products = productRepository.findByCtgCodeAndPriceBetween(ctgCode, min, max);
    for (ProductEntity product : products) {
      Long productId = product.getProductId();
      product.setThumbnailPath(imageService.getThumbnailPath(productId));
      product.setImagePaths(imageService.getAllImagePaths(productId));
    }
    return products;
  }

  // 이미지 경로 셋팅
  public ProductEntity findById(Long id) {
    ProductEntity product = productRepository.findById(id)
      .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다: " + id));

    product.setImagePaths(imageService.getAllImagePaths(id));
    product.setThumbnailPath(imageService.getThumbnailPath(id));

    return product;
  }


}
