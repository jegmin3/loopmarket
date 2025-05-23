package com.loopmarket.domain.pay.entity;

import com.loopmarket.domain.pay.enums.PaymentStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 안전결제 보류 상태를 저장하는 테이블 매핑 엔티티
 * 결제 완료 직후 보류 상태로 저장됨
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Long transactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    //상태를 정산 완료(COMPLETED)로 변경
    public void complete() {
        this.status = PaymentStatus.COMPLETED;
    }

    //상태를 환불 완료(REFUNDED)로 변경
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    //생성 시 기본 상태 설정용 팩토리 메서드
    public static Payment create(Long buyerId, Long sellerId, Long productId, int amount, Long transactionId) {
        return Payment.builder()
                .buyerId(buyerId)
                .sellerId(sellerId)
                .productId(productId)
                .amount(amount)
                .transactionId(transactionId)
                .status(PaymentStatus.HOLD)
                .createdAt(LocalDateTime.now())
                .build();
    }
}