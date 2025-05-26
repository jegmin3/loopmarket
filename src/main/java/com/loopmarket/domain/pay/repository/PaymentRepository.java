package com.loopmarket.domain.pay.repository;

import com.loopmarket.domain.pay.entity.Payment;
import com.loopmarket.domain.pay.enums.PaymentStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 결제 상태(Payment)를 관리하기 위한 JPA 레포지토리
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByProductIdAndStatusIn(Long productId, List<PaymentStatus> statuses);

}