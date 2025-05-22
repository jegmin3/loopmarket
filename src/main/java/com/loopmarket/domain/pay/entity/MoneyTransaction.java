package com.loopmarket.domain.pay.entity;

import lombok.*;
import javax.persistence.*;

import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;

import java.time.LocalDateTime;

@Entity
@Table(name = "money_transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 일단 연관관계 매핑 (@ManyToOne)하지 않고 userId만 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // 생성자: 거래 기록 저장 시 사용 (PayServiceImpl.java)
    public MoneyTransaction(Long userId, TransactionType type, int amount, TransactionStatus status, PaymentMethod method) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.createdAt = LocalDateTime.now();
    }
}