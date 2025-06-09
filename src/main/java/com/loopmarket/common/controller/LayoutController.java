package com.loopmarket.common.controller;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.repository.CategoryRepository;
import com.loopmarket.domain.location.service.LocationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LayoutController extends BaseController {

	private final CategoryRepository categoryRepository;
	private final LocationService locationService;

	// http://localhost:8080/
	@GetMapping("/")
	public String showMain(Model model) {
	  List<Category> mainCategories = categoryRepository.findMainCategories();
	  List<String> recommendedDongNames = locationService.getRecommendedDongNames();

	  model.addAttribute("mainCategories", mainCategories);
	  model.addAttribute("recommendedDongNames", recommendedDongNames);
    model.addAttribute("showSearchTitle", true);
	  // layout.html → th:insert="${viewName} :: content"
	  return render("main/index", model);
	}
}
