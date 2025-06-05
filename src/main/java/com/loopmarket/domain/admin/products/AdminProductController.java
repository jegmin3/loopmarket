package com.loopmarket.domain.admin.products;

import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    private String renderAdminPage(HttpServletRequest request, Model model, String viewName) {
        if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            return viewName + " :: content"; // fragment만 반환
        } else {
            return viewName;
        }
    }

    // 상품 관리 페이지 접근
//    @GetMapping
//    public String productAdminPage(HttpServletRequest request, Model model,
//                                   @RequestParam(defaultValue = "0") int page) {
//        int pageSize = 10;
//        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
//        Page<ProductEntity> productPage = productService.getProductsPage(pageable);
//
//        model.addAttribute("products", productPage.getContent());
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", productPage.getTotalPages());
//
//        return renderAdminPage(request, model, "admin/product_admin");
//    }
    @GetMapping
    public String productAdminPage(HttpServletRequest request, Model model) {
        return renderAdminPage(request, model, "admin/product_admin");
    }
    
}