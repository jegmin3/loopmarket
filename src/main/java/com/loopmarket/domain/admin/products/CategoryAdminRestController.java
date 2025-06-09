package com.loopmarket.domain.admin.products;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.entity.CategoryWithCountDTO;
import com.loopmarket.domain.category.service.CategoryService;

@RestController
@RequestMapping("/admin/api/categories")
public class CategoryAdminRestController {

    private final CategoryService categoryService;

    public CategoryAdminRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    

    // 1. 카테고리 목록 (기존)
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAllCategories(); 
    }
    
    // 2. 상품 수 포함 카테고리 목록
    @GetMapping("/details")
    public List<CategoryWithCountDTO> getCategoryWithProductCount() {
        return categoryService.findCategoriesWithProductCount();
    }
    
    // 3. 카테고리 추가
    // 상위 카테고리 코드 optional
    @PostMapping
    public ResponseEntity<?> addCategory(@RequestParam String name, @RequestParam(required = false) Integer upCtgCode) {
	    categoryService.addCategory(name, upCtgCode);
	    return ResponseEntity.ok().build();
    }

    // 4. 카테고리 삭제
    @DeleteMapping("/{ctgCode}")
    public ResponseEntity<?> deleteCategory(@PathVariable int ctgCode) {
        boolean deleted = categoryService.deleteCategoryIfNoProducts(ctgCode);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("해당 카테고리에 상품이 있어 삭제할 수 없습니다.");
        }
    }
}