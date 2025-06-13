package com.loopmarket.common.controller;

import org.springframework.ui.Model;

public class BaseController extends GetMemberInfo {
	/*
	 * 공통 렌더링 메서드.
	 * return layout/layout;
	 */
    protected String render(String viewName, Model model) {
        model.addAttribute("viewName", viewName);
        return "layout/layout";
    }
}