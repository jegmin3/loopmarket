package com.loopmarket.domain.product.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;       // 상품 관련 서비스
  private final CategoryRepository categoryRepository; // 카테고리 조회용 리포지토리

  /**
   * 전체 상품 목록 페이지 요청 처리
   *
   * @param model 뷰에 데이터 전달용 모델
   * @return 레이아웃 뷰 이름
   */
  @GetMapping("/products")
  public String showProductList(Model model) {
    List<ProductEntity> productList = productService.getAllProducts();  // 모든 상품 조회
    model.addAttribute("productList", productList);                     // 모델에 상품 리스트 담기

    model.addAttribute("viewName", "product/productList");              // 보여줄 뷰 지정
    return "layout/layout";                                             // 레이아웃 템플릿 호출
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
    HttpSession session) {

    // 로그인 사용자 정보 확인
    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
    if (loginUser == null) {
      return "redirect:/member/login";   // 로그인 안 했으면 로그인 페이지로 이동
    }

    // 상품에 로그인한 사용자 ID 설정
    product.setUserId(loginUser.getUserId().longValue());
    // 상품과 이미지 함께 저장 요청
    productService.registerProductWithImages(product, images, mainImageIndex);

    return "redirect:/products";          // 등록 후 상품 목록 페이지로 이동
  }
}
