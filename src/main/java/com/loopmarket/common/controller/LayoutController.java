package com.loopmarket.common.controller;

@Controller
@RequiredArgsConstructor
public class LayoutController extends BaseController {

    model.addAttribute("mainCategories", mainCategories);
    model.addAttribute("recommendedDongNames", recommendedDongNames);

    // layout.html → th:insert="${viewName} :: content"
    return render("main/index", model);
  }
}
