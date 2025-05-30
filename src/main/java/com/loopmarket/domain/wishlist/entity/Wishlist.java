package com.loopmarket.domain.wishlist.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 관심목록(찜 목록) 테이블 매핑 엔티티
 * 사용자가 특정 상품에 대해 찜한 내역을 저장
 */
@Entity
@Table(name = "wishlist", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"userId", "prodId"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishId; // 찜 고유 번호 (기본키, 자동 증가)

    @Column(nullable = false)
    private Long userId; // 찜한 사용자 ID

    @Column(nullable = false)
    private Long prodId; // 찜한 상품 ID

    @Column(nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt; // 찜 등록 시간 (자동 생성)
}