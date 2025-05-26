package com.loopmarket.domain.product.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
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

@Controller
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;       // 상품 관련 서비스
  private final CategoryRepository categoryRepository; // 카테고리 조회용 리포지토리
  private final MemberRepository memberRepository;

  /**
   * 전체 상품 목록 페이지 요청 처리
   *
   * @param model 뷰에 데이터 전달용 모델
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products")
  public String showProductList(@RequestParam(value = "category", required = false) String category,
                                @RequestParam(value = "search", required = false) String search, // ✅ 검색 추가
                                @RequestParam(value = "minPrice", required = false) Integer minPrice,
                                @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
                                Model model) {
    // 1. 카테고리 목록 (사이드바용)
    List<Category> mainCategories = categoryRepository.findMainCategories();
    model.addAttribute("mainCategories", mainCategories);

    List<ProductEntity> productList;
    List<Category> subCategories = null;

    // ✅ 검색어가 있으면 검색 우선 처리
    if (search != null && !search.isBlank()) {
      productList = productService.searchProductsByKeyword(search);
      model.addAttribute("productList", productList);
      model.addAttribute("viewName", "product/productList");
      return "layout/layout";
    }

    // 기본값 설정 (가격 없으면 전체 범위)
    if (minPrice == null) minPrice = 0;
    if (maxPrice == null) maxPrice = Integer.MAX_VALUE;

    if (category == null || category.equalsIgnoreCase("ALL") || category.isBlank()) {
      // 가격만 필터링
      productList = productService.getProductsByPriceRange(minPrice, maxPrice);
    } else {
      try {
        Integer categoryCode = Integer.parseInt(category);
        subCategories = categoryRepository.findByUpCtgCodeOrderBySeqAsc(categoryCode);

        if (!subCategories.isEmpty()) {
          // 대분류 → 여러 소분류 가져와서 필터링
          productList = productService.getProductsByMainCategoryAndPrice(categoryCode, minPrice, maxPrice);
        } else {
          // 소분류 → 단일 코드 + 가격으로 필터링
          productList = productService.getProductsByCategoryAndPrice(categoryCode, minPrice, maxPrice);
        }

        model.addAttribute("subCategories", subCategories);
        model.addAttribute("selectedMainCategory", categoryCode);
      } catch (NumberFormatException e) {
        productList = productService.getProductsByPriceRange(minPrice, maxPrice);
      }
    }

    model.addAttribute("productList", productList);
    model.addAttribute("viewName", "product/productList");
    return "layout/layout";
  }





  /**
   * 상품 등록 폼 페이지 요청 처리
   *
   * @param model 뷰에 데이터 전달용 모델
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products/new")
  public String showForm(Model model) {
    model.addAttribute("product", new ProductEntity());                 // 빈 상품 객체 생성

    // 대분류 카테고리 리스트 조회 (upCtgCode가 null인 카테고리)
    List<Category> mainCategories = categoryRepository.findMainCategories();
    model.addAttribute("mainCategories", mainCategories);              // 모델에 대분류 카테고리 추가

    model.addAttribute("viewName", "product/productForm");             // 뷰 이름 지정
    return "layout/layout";                                            // 레이아웃 호출
  }

  /**
   * 상품 등록 요청 처리 (폼에서 POST)
   *
   * @param product 상품 데이터
   * @param images 업로드된 이미지 파일 리스트
   * @param mainImageIndex 대표 이미지 인덱스
   * @param session 로그인 정보 세션
   * @return 등록 후 상품 목록 페이지 리다이렉트
   */
  @PostMapping("/products")
  public String register(
    @ModelAttribute ProductEntity product,
    @RequestParam("images") List<MultipartFile> images,
    @RequestParam("mainImageIndex") int mainImageIndex,
    @RequestParam("latitude") Double latitude,
    @RequestParam("longitude") Double longitude,
    HttpSession session) {

    // 로그인 사용자 정보 확인
    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/member/login";   // 로그인 안 했으면 로그인 페이지로 이동
    }

    // 상품에 로그인한 사용자 ID 설정
    product.setUserId(loginUser.getUserId().longValue());
    // 좌표 저장
    product.setLatitude(latitude);
    product.setLongitude(longitude);
    // 상품과 이미지 함께 저장 요청
    productService.registerProductWithImages(product, images, mainImageIndex);

    return "redirect:/products";          // 등록 후 상품 목록 페이지로 이동
  }


  //상품 상세
  public String formatRelativeTime(LocalDateTime createdAt) {
    Duration duration = Duration.between(createdAt, LocalDateTime.now());
    long hours = duration.toHours();
    long days = duration.toDays();
    if (hours < 1) return "방금 전";
    else if (hours < 24) return hours + "시간 전";
    else if (days < 2) return "1일 전";
    else return days + "일 전";
  }
  @GetMapping("/products/{id}")
  public String showProductDetail(@PathVariable Long id, Model model) {
    ProductEntity product = productService.findById(id);

    // 사용자 조회
    MemberEntity seller = memberRepository.findById(product.getUserId().intValue()).orElse(null);
    if (seller != null) {
      product.setSellerNickname(seller.getNickname());
    }

    // 상대 시간 계산
    product.setRelativeTime(formatRelativeTime(product.getCreatedAt()));

    model.addAttribute("product", product);
    model.addAttribute("viewName", "product/productDetail");
    return "layout/layout";
  }


}
