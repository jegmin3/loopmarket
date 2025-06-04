package com.loopmarket.domain.admin.products;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.service.CategoryService;
import com.loopmarket.domain.product.dto.CategoryDTO;

@RestController
@RequestMapping("/admin/api/categories")
public class CategoryAdminRestController {

    private final CategoryService categoryService;

    public CategoryAdminRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAllCategories(); 
    }
}