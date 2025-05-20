package com.loopmarket;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController extends BaseController {
	
	// http://localhost:8080/
    @GetMapping("/")
    public String layout(Model model) {
        return render("main/index", model); // 항상 layout.html만 렌더링됨
    }
}

