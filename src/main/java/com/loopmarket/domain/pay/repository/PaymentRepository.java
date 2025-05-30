package com.loopmarket.domain.pay.repository;

import com.loopmarket.domain.pay.entity.Payment;
import com.loopmarket.domain.pay.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 결제 상태(Payment)를 관리하기 위한 JPA 레포지토리
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	// 특정 상품에 대해 주어진 상태 중 하나라도 존재하는지 확인
	boolean existsByProductIdAndStatusIn(Long productId, List<PaymentStatus> statuses);

	// 주어진 상태이면서 특정 시점 이전에 생성된 결제 목록 조회
	List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime limit);

	// 특정 구매자에 대한 HOLD 상태의 결제 목록 반환
	List<Payment> findByBuyerIdAndStatus(Long buyerId, PaymentStatus status);

	// 특정 구매자의 결제 중, 상태가 HOLD 또는 COMPLETED 인 결제 내역 조회
	List<Payment> findByBuyerIdAndStatusIn(Long buyerId, List<PaymentStatus> statuses);
}