package com.loopmarket.domain.product.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @GetMapping("/products")
    public String showProductList(Model model) {
        List<ProductEntity> productList = productService.getAllProducts();
        model.addAttribute("productList", productList);

        model.addAttribute("viewName", "product/productList");
        //layout.html 기준으로 조립해서 보여주기
        return "layout/layout";
    }

    @GetMapping("/products/new")
    public String showForm(Model model) {
        model.addAttribute("product", new ProductEntity());

        // 💡 대분류는 upCtgCode == null
        List<Category> mainCategories = categoryRepository.findMainCategories();
        model.addAttribute("mainCategories", mainCategories);

        model.addAttribute("viewName", "product/productForm");
        return "layout/layout";
    }


    @PostMapping("/products")
    public String register(@ModelAttribute ProductEntity product, HttpSession session) {
        Long loginUserId = (Long) session.getAttribute("loginUserId");
        product.setUserId(loginUserId);
        productService.registerProduct(product);
        return "redirect:/products";
    }
}

