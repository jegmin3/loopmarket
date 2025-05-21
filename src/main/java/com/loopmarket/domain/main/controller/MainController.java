package com.loopmarket.domain.main.controller;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.location.service.LocationService;
import com.loopmarket.common.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController extends BaseController {

  private final CategoryRepository categoryRepository;
  private final LocationService locationService;

  @GetMapping("/main")
  public String showMain(Model model) {
    List<Category> mainCategories = categoryRepository.findMainCategories();
    List<String> recommendedDongNames = locationService.getRecommendedDongNames();

    model.addAttribute("mainCategories", mainCategories);
    model.addAttribute("recommendedDongNames", recommendedDongNames);

    // layout.html → th:insert="${viewName} :: content"
    return render("main/index", model);
  }
}
