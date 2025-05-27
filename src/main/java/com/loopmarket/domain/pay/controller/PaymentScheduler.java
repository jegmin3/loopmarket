package com.loopmarket.domain.pay.controller;

import com.loopmarket.domain.pay.entity.Payment;
import com.loopmarket.domain.pay.enums.PaymentStatus;
import com.loopmarket.domain.pay.repository.PaymentRepository;
import com.loopmarket.domain.pay.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 상태 스케줄러
 *
 * 일정 주기로 결제 상태를 점검하고 자동으로 구매 확정을 처리하는 역할을 수행합니다.
 * - 10일 이상 HOLD 상태인 결제를 자동으로 COMPLETED 처리하여 판매자에게 금액을 지급합니다.
 */
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

	private final PaymentRepository paymentRepository;
	private final PayService payService;

	@Scheduled(cron = "0 0 2 * * *")
	public void autoCompletePayments() {
		LocalDateTime limit = LocalDateTime.now().minusDays(10);

		List<Payment> expired = paymentRepository.findByStatusAndCreatedAtBefore(PaymentStatus.HOLD, limit);

		for (Payment payment : expired) {
			try {
				payService.completePay(payment.getPaymentId(), payment.getBuyerId());
			} catch (Exception ignored) {
			}
		}
	}
}