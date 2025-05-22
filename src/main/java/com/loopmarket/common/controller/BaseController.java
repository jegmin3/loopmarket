package com.loopmarket.common.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.loopmarket.domain.member.MemberEntity;

public class BaseController {
	
	@Autowired(required = false)
	protected HttpSession session;
	
	/**
	 * 공통 렌더링 메서드
	 */
    protected String render(String viewName, Model model) {
        model.addAttribute("viewName", viewName);
        return "layout/layout";
    }
    
    // 로그인한 사용자 엔티티 정보 반환
    // => 나중엔 로그인시 Entity를 DTO로 변환해서 세션에 저장하고,
    protected MemberEntity getLoginUser() {
        return (MemberEntity) session.getAttribute("loginUser");
    }

    // 로그인한 사용자 이메일 등 ID성 키 반환
    protected String getLoginEmail() {
        MemberEntity loginUser = getLoginUser();
        return loginUser != null ? loginUser.getEmail() : null;
    }

    protected String getLoginNickname() {
        MemberEntity loginUser = getLoginUser();
        return loginUser != null ? loginUser.getNickname() : null;
    }
    
}