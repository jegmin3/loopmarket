package com.loopmarket.domain.main.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
  private final CategoryRepository categoryRepository;

  // 루트 페이지도 템플릿 렌더링 하도록 연결
  @GetMapping("/")
  public String showMain(Model model) {

    List<Category> mainCategories = categoryRepository.findMainCategories();
    model.addAttribute("mainCategories", mainCategories);

    System.out.println("카테고리 수 = " + mainCategories.size());

    for (Category category : mainCategories) {
      System.out.println("카테고리 이름 = " + category.getCtgName());
    }

    model.addAttribute("viewName", "main/index"); // 본문 조각 뷰 경로
    return "layout/layout"; // 전체 레이아웃
  }
}


