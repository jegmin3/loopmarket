package com.loopmarket;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController {
	
	// http://localhost:8080/
    @GetMapping("/")
    public String layout() {
        return "layout/layout"; // 항상 layout.html만 렌더링됨
    }
}

