package com.loopmarket.domain.admin.user;

import lombok.*;
import javax.persistence.*;

import com.loopmarket.domain.member.MemberEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_status")
public class UserStatusEntity {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // userId를 MemberEntity의 PK와 매핑
    @JoinColumn(name = "user_id")
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "last_modified", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime lastModified;

    public enum AccountStatus {
        ACTIVE,
        SUSPENDED,
        DELETED
    }
}