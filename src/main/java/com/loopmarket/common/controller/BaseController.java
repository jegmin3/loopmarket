package com.loopmarket.common.controller;

import javax.servlet.http.HttpSession;

import org.springframework.ui.Model;

import com.loopmarket.domain.member.dto.MemberDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BaseController {

	protected final HttpSession session;
	
	/**
	 * 공통 렌더링 메서드
	 */
    protected String render(String viewName, Model model) {
        model.addAttribute("viewName", viewName);
        return "layout/layout";
    }
    
    // 로그인한 사용자 DTO 반환
    protected MemberDTO getLoginUser() {
        return (MemberDTO) session.getAttribute("loginUser");
    }

    // 로그인한 사용자 이메일 등 ID성 키 반환
    protected String getLoginEmail() {
        MemberDTO loginUser = getLoginUser();
        return loginUser != null ? loginUser.getEmail() : null;
    }

    protected String getLoginNickname() {
        MemberDTO loginUser = getLoginUser();
        return loginUser != null ? loginUser.getNickname() : null;
    }
    
}
