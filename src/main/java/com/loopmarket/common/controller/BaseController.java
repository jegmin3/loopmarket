package com.loopmarket.common.controller;

import org.springframework.ui.Model;

public abstract class BaseController {

	/**
	 * 공통 렌더링 메서드
	 */
    protected String render(String viewName, Model model) {
        model.addAttribute("viewName", viewName);
        return "layout/layout";
    }
}

