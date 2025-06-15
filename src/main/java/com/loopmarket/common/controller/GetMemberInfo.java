package com.loopmarket.common.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.loopmarket.domain.member.MemberEntity;

/*
 * 사용자 상태를 가져오는 메서드를 정의한 
 * 클래스 (BaseController가 상속하고 있습니다)
 */
public class GetMemberInfo {
	
	@Autowired(required = false)
	protected HttpSession session;

	// => 나중엔 로그인시 Entity를 DTO로 변환해서 세션에 저장하고,
    /** 로그인한 사용자 엔티티 정보 반환 */
    protected MemberEntity getLoginUser() {
        return (MemberEntity) session.getAttribute("loginUser");
    }

    /** 로그인한 사용자의 이메일 반환 */
    protected String getLoginEmail() {
        MemberEntity loginUser = getLoginUser();
        return loginUser != null ? loginUser.getEmail() : null;
    }
    
    /** 로그인한 사용자의 닉네임 반환 */
    protected String getLoginNickname() {
        MemberEntity loginUser = getLoginUser();
        return loginUser != null ? loginUser.getNickname() : null;
    }
}
