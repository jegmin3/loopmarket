package com.loopmarket.common.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController extends BaseController {
	
	public LayoutController(HttpSession session) {
		super(session);
	}

	// http://localhost:8080
    @GetMapping("/")
    public String index(Model model) {
        return render("main/index", model);
    }

    
    
}

