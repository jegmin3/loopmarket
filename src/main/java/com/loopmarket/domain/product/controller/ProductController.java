package com.loopmarket.domain.product.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;

import com.loopmarket.domain.category.service.CategoryService;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 상품 관련 요청을 처리하는 컨트롤러 클래스
 */
@Controller
@RequiredArgsConstructor
public class ProductController {

  // 상품 서비스 주입 (비즈니스 로직 처리용)
  private final ProductService productService;
  // 카테고리 조회용 리포지토리
  private final CategoryRepository categoryRepository;
  // 회원 정보 조회용 리포지토리
  private final MemberRepository memberRepository;
  
  // 상품 수정용 서비스
  private final CategoryService categoryService;
  
  private final ImageService imageService;

  /**
   * 전체 상품 목록 페이지를 보여줍니다.
   * 카테고리, 가격 범위, 검색어 기준으로 필터링 기능을 제공합니다.
   *
   * @param category    선택된 카테고리 코드 (대분류 혹은 소분류)
   * @param search      검색어 (상품 제목/설명)
   * @param minPrice    최소 가격 필터 (없으면 0)
   * @param maxPrice    최대 가격 필터 (없으면 제한 없음)
   * @param model       뷰로 전달할 모델
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products")
  public String showProductList(
    @RequestParam(value = "category", required = false) String category,
    @RequestParam(value = "search", required = false) String search,
    @RequestParam(value = "minPrice", required = false) Integer minPrice,
    @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
    Model model) {
    // 사이드바용 대분류 카테고리 조회
    List<Category> mainCategories = categoryRepository.findMainCategories();
    model.addAttribute("mainCategories", mainCategories);

    List<ProductEntity> productList;
    List<Category> subCategories = null;

    // 검색어가 있으면 검색 결과만 보여주고 종료
    if (search != null && !search.isBlank()) {
      productList = productService.searchProductsByKeyword(search);
      model.addAttribute("productList", productList);
      model.addAttribute("viewName", "product/productList");
      return "layout/layout";
    }

    // 가격 필터 기본값 설정
    if (minPrice == null) minPrice = 0;
    if (maxPrice == null) maxPrice = Integer.MAX_VALUE;

    // 카테고리 필터 처리
    if (category == null || category.equalsIgnoreCase("ALL") || category.isBlank()) {
      // 카테고리 미선택 시, 가격 범위만 필터링
      productList = productService.getProductsByPriceRange(minPrice, maxPrice);
    } else {
      try {
        Integer categoryCode = Integer.parseInt(category);
        // 대분류 코드로 하위 카테고리 조회
        subCategories = categoryRepository.findByUpCtgCodeOrderBySeqAsc(categoryCode);

        if (!subCategories.isEmpty()) {
          // 대분류 선택: 하위 소분류 모두 포함하여 필터링
          productList = productService.getProductsByMainCategoryAndPrice(categoryCode, minPrice, maxPrice);
        } else {
          // 소분류 선택: 단일 카테고리 코드로 필터링
          productList = productService.getProductsByCategoryAndPrice(categoryCode, minPrice, maxPrice);
        }

        model.addAttribute("subCategories", subCategories);
        model.addAttribute("selectedMainCategory", categoryCode);
      } catch (NumberFormatException e) {
        // 카테고리 파라미터가 숫자가 아닐 경우, 가격만 필터링
        productList = productService.getProductsByPriceRange(minPrice, maxPrice);
      }
    }

    // 최종 상품 목록 및 뷰 이름 설정
    model.addAttribute("productList", productList);
    model.addAttribute("viewName", "product/productList");
    return "layout/layout";
  }

  /**
   * 상품 등록 폼 페이지를 보여줍니다.
   * 대분류 카테고리 리스트를 모델에 담아 전달합니다.
   *
   * @param model 모델 객체
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products/new")
  public String showForm(Model model) {
    model.addAttribute("product", new ProductEntity());
    List<Category> mainCategories = categoryRepository.findMainCategories();
    model.addAttribute("mainCategories", mainCategories);
    model.addAttribute("viewName", "product/productForm");
    return "layout/layout";
  }

  /**
   * 상품 등록 요청 처리 메서드
   * 업로드된 이미지, 대표 이미지 인덱스, 좌표 정보를 함께 처리하여 저장합니다.
   *
   * @param product          등록할 상품 엔티티
   * @param images           업로드된 이미지 리스트
   * @param mainImageIndex   대표 이미지로 사용할 인덱스
   * @param latitude         상품 위치 위도
   * @param longitude        상품 위치 경도
   * @param session          HttpSession에서 로그인 정보 획득
   * @return 상품 목록 페이지로 리다이렉트
   */
  @PostMapping("/products")
  public String register(
    @ModelAttribute ProductEntity product,
    @RequestParam("images") List<MultipartFile> images,
    @RequestParam("mainImageIndex") int mainImageIndex,
    @RequestParam("latitude") Double latitude,
    @RequestParam("longitude") Double longitude,
    HttpSession session) {
    // 세션에서 로그인 사용자 정보 확인
    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
    if (loginUser == null) {
      // 로그인 안된 상태면 로그인 페이지로 이동
      return "redirect:/member/login";
    }

    // 상품에 로그인한 사용자 ID 설정
    product.setUserId(loginUser.getUserId().longValue());
    // 좌표 정보 설정
    product.setLatitude(latitude);
    product.setLongitude(longitude);
    // 서비스 호출하여 상품 및 이미지 저장
    productService.registerProductWithImages(product, images, mainImageIndex);

    return "redirect:/products";
  }

  /**
   * 생성 시간(createdAt)을 기준으로 상대 시간을 문자열로 변환합니다.
   * 예: "방금 전", "3시간 전", "1일 전"
   *
   * @param createdAt 상품 생성 시점
   * @return 포맷된 상대 시간 문자열
   */
  public String formatRelativeTime(LocalDateTime createdAt) {
    Duration duration = Duration.between(createdAt, LocalDateTime.now());
    long hours = duration.toHours();
    long days = duration.toDays();
    if (hours < 1) return "방금 전";
    else if (hours < 24) return hours + "시간 전";
    else if (days < 2) return "1일 전";
    else return days + "일 전";
  }

  /**
   * 상품 상세 페이지를 보여줍니다.
   * 판매자 닉네임 설정 및 상대 시간 계산 후 뷰로 전달합니다.
   *
   * @param id    조회할 상품 ID
   * @param model 뷰 모델
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products/{id}")
  public String showProductDetail(@PathVariable Long id, Model model) {
    // 상품 조회
    ProductEntity product = productService.findById(id);

    // 판매자 정보 조회 후 닉네임 설정
    MemberEntity seller = memberRepository.findById(product.getUserId().intValue()).orElse(null);
    if (seller != null) {
      product.setSellerNickname(seller.getNickname());
      
      	// 프로필 이미지 경로 생성 후 모델에 추가
      String profileImagePath = imageService.getProfilePath(seller.getUserId());
      model.addAttribute("profileImagePath", profileImagePath);
    }

    // 상대 시간 계산하여 설정
    product.setRelativeTime(formatRelativeTime(product.getCreatedAt()));

    // 모델에 상품 데이터 및 뷰 이름 추가
    model.addAttribute("product", product);
    model.addAttribute("viewName", "product/productDetail");
    return "layout/layout";
  }
  
  // 상품 수정 페이지
  @GetMapping("/products/edit/{id}")
  public String editProductForm(@PathVariable Long id, Model model) {
      ProductEntity product = productService.getProductById(id);
      model.addAttribute("product", product);
      model.addAttribute("mainCategories", categoryService.getMainCategories());
      model.addAttribute("viewName", "product/product_edit_form");
//      model.addAttribute("mainCategoryCode", mainCategoryCode);
      return "layout/layout";
  }
  
}
