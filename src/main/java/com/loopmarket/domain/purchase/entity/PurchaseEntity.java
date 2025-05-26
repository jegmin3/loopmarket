package com.loopmarket.domain.purchase.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase")
@Data
public class PurchaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long purchaseId; // 구매 내역 고유 ID (PK)

    private Long productId;  // 구매한 상품 ID (products 테이블 FK)
    private Long buyerId;    // 구매자 ID (users 테이블 FK)

    private LocalDateTime purchaseDate; // 구매 시점

    private String status;   // 구매 상태 (PENDING, COMPLETED, CANCELLED)
}