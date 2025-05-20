package com.loopmarket;

import org.springframework.ui.Model;

public abstract class BaseController {
	
	protected String render(String viewName, Model model) {
		model.addAttribute("viewName", viewName);
		return "layout/layout";
	}
	

}
