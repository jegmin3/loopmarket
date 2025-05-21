package com.loopmarket.domain.member.dto;

import java.time.LocalDateTime;

import com.loopmarket.domain.member.MemberEntity;

import lombok.*;

/** 로그인 후 세션에 저장할 회원 정보.
 *  password는 저장하지 말고, 필요시 다른 DTO를 사용해 MeberDTO정보중
 *  제한적으로 사용하도록 합시당 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private Integer userId;
    private String email;
    private String nickname;

    private String loginType;
    private String role;
    private String status;

    private Integer profileImgId;
    private LocalDateTime lastLoginAt;
    
    /** Entity에서 DTO로 변환시 사용할 메서드 */
    public static MemberDTO fromEntity(MemberEntity member) {
        return MemberDTO.builder()
            .userId(member.getUserId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .loginType(member.getLoginType().name())
            .role(member.getRole().name())
            .status(member.getStatus().name())
            .profileImgId(member.getProfileImgId())
            .lastLoginAt(member.getLastLoginAt())
            .build();
    }

}

