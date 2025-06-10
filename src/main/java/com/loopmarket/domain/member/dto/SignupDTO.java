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
    // 이메일 인증시 verifired 추가할랬는데
    // 그냥 인증하면 가입 요청이 가능한 형태로만 구현하고
    // 따로 verifired=true같은 처리는 안했습니다
}
