package com.loopmarket.domain.category.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor

public class CategoryController {

  private final CategoryRepository categoryRepository;

  // [REST] 대분류 선택 → 소분류 목록 응답
  @GetMapping("/api/categories/{mainCode}")
  @ResponseBody
  public List<Category> getSubCategories(@PathVariable Integer mainCode) {
    return categoryRepository.findByUpCtgCodeOrderBySeqAsc(mainCode);
  }
}
