package com.loopmarket.domain.product.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.category.service.CategoryService;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.location.service.LocationService;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;
  private final CategoryRepository categoryRepository;
  private final MemberRepository memberRepository;
  private final CategoryService categoryService;
  private final ImageService imageService;
  private final LocationService locationService;

  // 상품 목록 필터링 (카테고리, 가격, 검색어, 위치 기반)
  @GetMapping("/products")
  public String showProductList(
    @RequestParam(value = "category", required = false) String category,
    @RequestParam(value = "search", required = false) String search,
    @RequestParam(value = "minPrice", required = false) Integer minPrice,
    @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
    @RequestParam(value = "lat", required = false) Double lat,
    @RequestParam(value = "lng", required = false) Double lng,
    Model model) {

    int min = (minPrice != null) ? minPrice : 0;
    int max = (maxPrice != null) ? maxPrice : Integer.MAX_VALUE;

    List<ProductEntity> productList;
    List<Category> subCategories = null;

    try {
      if (lat != null && lng != null && category != null && !category.isBlank()) {
        Integer categoryCode = Integer.parseInt(category);
        subCategories = categoryRepository.findByUpCtgCodeOrderBySeqAsc(categoryCode);

        if (!subCategories.isEmpty()) {
          productList = productService.getNearbyProductsByMainCategory(categoryCode, lat, lng, min, max);
        } else {
          productList = productService.getNearbyProductsByCategory(categoryCode, lat, lng, min, max);
        }

        model.addAttribute("subCategories", subCategories);
        model.addAttribute("selectedMainCategory", categoryCode);
      } else if (lat != null && lng != null) {
        productList = productService.getNearbyProducts(lat, lng).stream()
          .filter(p -> p.getPrice() != null && p.getPrice() >= min && p.getPrice() <= max)
          .collect(Collectors.toList());
      } else if (search != null && !search.isBlank()) {
        productList = productService.searchProductsByKeyword(search);
      } else if (category == null || category.equalsIgnoreCase("ALL") || category.isBlank()) {
        productList = productService.getProductsByPriceRange(min, max);
      } else {
        Integer categoryCode = Integer.parseInt(category);
        subCategories = categoryRepository.findByUpCtgCodeOrderBySeqAsc(categoryCode);

        if (!subCategories.isEmpty()) {
          productList = productService.getProductsByMainCategoryAndPrice(categoryCode, min, max);
        } else {
          productList = productService.getProductsByCategoryAndPrice(categoryCode, min, max);
        }

        model.addAttribute("subCategories", subCategories);
        model.addAttribute("selectedMainCategory", categoryCode);
      }
    } catch (NumberFormatException e) {
      productList = productService.getProductsByPriceRange(min, max);
    }

    model.addAttribute("mainCategories", categoryRepository.findMainCategories());
    model.addAttribute("recommendedDongNames", locationService.getRecommendedDongNames());
    productList.sort(Comparator.comparing(ProductEntity::getCreatedAt).reversed());

    // 각 상품마다 상대 시간, 동네명 설정
    productList.forEach(p -> {
      p.setRelativeTime(formatRelativeTime(p.getCreatedAt()));
      p.setDongName(extractDongName(p.getLocationText()));
    });

    model.addAttribute("productList", productList);
    model.addAttribute("viewName", "product/productList");

    return "layout/layout";
  }

  // 상품 등록 폼
  @GetMapping("/products/new")
  public String showForm(Model model) {
    model.addAttribute("product", new ProductEntity());
    model.addAttribute("mainCategories", categoryRepository.findMainCategories());
    model.addAttribute("viewName", "product/productForm");
    return "layout/layout";
  }

  // 상품 등록 처리
  @PostMapping("/products")
  public String register(
    @ModelAttribute ProductEntity product,
    @RequestParam("images") List<MultipartFile> images,
    @RequestParam("mainImageIndex") int mainImageIndex,
    @RequestParam("latitude") Double latitude,
    @RequestParam("longitude") Double longitude,
    HttpSession session) {

    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
    if (loginUser == null) return "redirect:/member/login";

    product.setUserId(loginUser.getUserId().longValue());
    product.setLatitude(latitude);
    product.setLongitude(longitude);
    product.setIsHidden(false);

    productService.registerProductWithImages(product, images, mainImageIndex);
    return "redirect:/products";
  }

  // 상대 시간 계산
  public String formatRelativeTime(LocalDateTime createdAt) {
    Duration duration = Duration.between(createdAt, LocalDateTime.now());
    long hours = duration.toHours();
    long days = duration.toDays();
    if (hours < 1) return "방금 전";
    else if (hours < 24) return hours + "시간 전";
    else if (days < 2) return "1일 전";
    else return days + "일 전";
  }

  // 상품 상세 페이지
  @GetMapping("/products/{id}")
  public String showProductDetail(@PathVariable Long id, Model model) {
    ProductEntity product = productService.findById(id);

    MemberEntity seller = memberRepository.findById(product.getUserId().intValue()).orElse(null);
    if (seller != null) {
      product.setSellerNickname(seller.getNickname());
      model.addAttribute("profileImagePath", imageService.getProfilePath(seller.getUserId()));
    }

    product.setRelativeTime(formatRelativeTime(product.getCreatedAt()));

    // 동네명 추출해서 모델에 추가
    String dongName = extractDongName(product.getLocationText());
    model.addAttribute("dongName", dongName);

    model.addAttribute("product", product);
    model.addAttribute("viewName", "product/productDetail");
    return "layout/layout";
  }

  public String extractDongName(String locationText) {
    if (locationText == null || locationText.isBlank()) return "";
    String[] parts = locationText.trim().split(" ");
    return parts.length >= 2 ? parts[0] + " " + parts[1] : locationText;
  }



  // 상품 수정 폼
  @GetMapping("/products/edit/{id}")
  public String editProductForm(@PathVariable Long id, Model model) {
    ProductEntity product = productService.getProductById(id);
    product.setImagePaths(imageService.getAllImagePaths(id));
    Integer mainCategoryCode = categoryService.findMainCategoryCodeBySubCode(product.getCtgCode());

    model.addAttribute("product", product);
    model.addAttribute("mainCategoryCode", mainCategoryCode);
    model.addAttribute("mainCategories", categoryService.getMainCategories());
    model.addAttribute("subCategories", categoryService.getSubCategories(mainCategoryCode));
    model.addAttribute("viewName", "product/product_edit_form");
    return "layout/layout";
  }

  // 상품 수정 처리
  @PostMapping("/products/{id}")
  public String updateProduct(
    @PathVariable Long id,
    @ModelAttribute ProductEntity product,
    @RequestParam("images") List<MultipartFile> images,
    @RequestParam("mainImageIndex") int mainImageIndex,
    @RequestParam("latitude") Double latitude,
    @RequestParam("longitude") Double longitude,
    HttpSession session) {

    MemberEntity loginUser = (MemberEntity) session.getAttribute("loginUser");
    if (loginUser == null) return "redirect:/member/login";

    product.setUserId(loginUser.getUserId().longValue());
    product.setLatitude(latitude);
    product.setLongitude(longitude);

    productService.updateProductWithImages(id, product, images, mainImageIndex);
    return "redirect:/products/" + id;
  }
}
