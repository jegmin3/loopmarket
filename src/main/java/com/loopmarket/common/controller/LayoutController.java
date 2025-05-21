package com.loopmarket.common.controller;

import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class LayoutController extends BaseController {

  private final CategoryRepository categoryRepository;
  private final LocationService locationService;

  // http://localhost:8080/
  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("mainCategories", categoryRepository.findMainCategories());
    model.addAttribute("recommendedDongNames", locationService.getRecommendedDongNames());
    return render("main/index", model);
  }
}
