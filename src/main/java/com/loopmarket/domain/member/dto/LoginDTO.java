package com.loopmarket.domain.member.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인시 클라이언트가 보내는 정보 받는 용도 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {
	
	@NotBlank
	@Email
    private String email;
    private String password;
}
