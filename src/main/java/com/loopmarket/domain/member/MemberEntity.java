package com.loopmarket.domain.member;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.*;

/** 엔티티는 DB와 연결, 저장/조회만 하도록..
 *  즉 내부용 입니다. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String email;

    // 기본값을 builder에도 반영
    @Builder.Default
    private Boolean emailVerified = false;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final LoginType loginType = LoginType.NORMAL;

    private String socialId;

    private String name;

    @Column(nullable = false)
    private String nickname;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    private LocalDateTime deletedAt;

    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;

    private String profileImgId;

    private String phoneNumber;

    private LocalDateTime lastLoginAt;
    
    // entity -> dto 변환해야 하는데..ㅠ
    @Column(name = "fcm_token", length = 255) // FCM 토큰은 긴 문자열이므로 충분한 길이 설정
    private String fcmToken; // 사용자의 FCM 기기 토큰

    public enum LoginType {
        NORMAL, GOOGLE, KAKAO, NAVER
    }

    public enum Role {
        USER, BUSINESS, MANAGER, ADMIN
    }

    public enum Status {
        ACTIVE, DELETED, SUSPENDED
    }
}


