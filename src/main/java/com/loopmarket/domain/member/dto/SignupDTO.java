package com.loopmarket.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원가입시 사용할 DTO */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupDTO {
    private String email;
    private String password;
    private String nickname;
    // 나중에 추가 할것은 추가해요
}
