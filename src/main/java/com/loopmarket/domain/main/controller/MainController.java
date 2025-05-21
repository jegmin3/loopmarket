package com.loopmarket.domain.main.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.loopmarket.domain.location.entity.Location;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
  private final CategoryRepository categoryRepository;
  private final LocationService locationService;

  // 루트 페이지도 템플릿 렌더링 하도록 연결
  @GetMapping("/")
  public String showMain(Model model) {
    List<Category> mainCategories = categoryRepository.findMainCategories();
    List<String> recommendedDongNames = locationService.getRecommendedDongNames();

    model.addAttribute("mainCategories", mainCategories);
    model.addAttribute("recommendedDongNames", recommendedDongNames); // 💡 동 이름만 전달
    model.addAttribute("viewName", "main/index");
    return "layout/layout";
  }

}


