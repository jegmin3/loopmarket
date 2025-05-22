package com.loopmarket.domain.pay.service;

import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.entity.Payment;
import com.loopmarket.domain.pay.entity.UserMoney;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;
import com.loopmarket.domain.pay.repository.MoneyTransactionRepository;
import com.loopmarket.domain.pay.repository.PaymentRepository;
import com.loopmarket.domain.pay.repository.UserMoneyRepository;
import lombok.RequiredArgsConstructor;

import org.hibernate.type.TrueFalseType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

	private final UserMoneyRepository userMoneyRepository;
	private final MoneyTransactionRepository moneyTransactionRepository;
    private final PaymentRepository paymentRepository;

	/**
	 * 페이 충전 : 포트원 결제 성공 이후 -> 잔액 증가 + 거래 기록
	 */
	@Transactional
	@Override
	public void charge(Long userId, int amount, PaymentMethod method) {
		// 비정상 금액 충전 방어
		if (amount <= 0 || amount > 1_000_000) {
			throw new IllegalArgumentException("충전 금액은 1원 이상 100만원 이하로만 가능합니다.");
		}

		// 1. 사용자 잔액 조회 또는 신규 생성
		UserMoney userMoney = userMoneyRepository.findById(userId).orElse(new UserMoney(userId, 0));

		// 총 잔액이 천만원을 초과하지 않도록 제한
		int balance = userMoney.getBalance();
		if (balance + amount > 10_000_000) {
			throw new IllegalArgumentException("총 잔액은 천만원을 초과할 수 없습니다.");
		}

		// 2. 잔액 증가 후 저장
		userMoney.charge(amount);
		userMoneyRepository.save(userMoney);

		// 3. 거래 기록 저장
		MoneyTransaction transaction = new MoneyTransaction(
				userId, 
				TransactionType.CHARGE, 
				amount,
				TransactionStatus.SUCCESS, 
				method
		);
		moneyTransactionRepository.save(transaction);
	}

	/**
	 * 페이 환불 : 사용자 페이 잔액 환불 요청
	 */
	@Transactional
	@Override
	public void refund(Long userId, int amount) {
		// 1. 사용자 잔액 조회
		UserMoney userMoney = userMoneyRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자의 잔액 정보가 없습니다."));

		// 2. 잔액 차감 후 저장
		userMoney.refund(amount);
		userMoneyRepository.save(userMoney);

		// 3. 거래 기록 저장
		MoneyTransaction transaction = new MoneyTransaction(
				userId, 
				TransactionType.REFUND, 
				amount,
				TransactionStatus.SUCCESS, 
				PaymentMethod.PAY
		);
		moneyTransactionRepository.save(transaction);
	}

	/**
	 * 충전/환불 등 잔액이 변경됐을 때 실시간으로 변경된 잔액 출력을 위한 메서드
	 * -> 잔액 조회 API 에서 사용
	 */
	@Override
	@Transactional(readOnly = true)
	public int getBalance(Long userId) {
		return userMoneyRepository.findById(userId).map(UserMoney::getBalance).orElse(0);
	}
	
	/**
	 * 안전결제 처리
	 */
	@Override
	@Transactional
	public Long safePay(Long buyerId, Long sellerId, Long productId, int amount) {
	    // 1. 구매자 잔액 조회 및 차감
	    UserMoney userMoney = userMoneyRepository.findById(buyerId)
	        .orElseThrow(() -> new IllegalArgumentException("잔액 정보가 존재하지 않습니다."));
	    userMoney.refund(amount);

	    // 2. 거래 기록 저장 (출금)
	    MoneyTransaction tx = new MoneyTransaction(
	        buyerId,
	        TransactionType.SAFE_PAY,
	        amount,
	        TransactionStatus.SUCCESS,
	        PaymentMethod.PAY
	    );
	    moneyTransactionRepository.save(tx);

	    // 3. 결제 보류 정보 저장
	    Payment payment = Payment.create(
	        buyerId,
	        sellerId,
	        productId,
	        amount,
	        tx.getTransactionId()
	    );
	    paymentRepository.save(payment);
	    return payment.getPaymentId();
	}
	
	/**
	 * 구매 확정 처리
	 */
	@Override
	@Transactional
	public int completePay(Long paymentId) {
	    // 1. 결제 정보 조회
	    Payment payment = paymentRepository.findById(paymentId)
	            .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

	    // 2. 상태 변경
	    payment.complete();  // status → COMPLETED

	    // 3. 판매자 잔액 업데이트
	    UserMoney sellerMoney = userMoneyRepository.findById(payment.getSellerId())
	            .orElse(new UserMoney(payment.getSellerId(), 0)); // 없으면 새로 생성
	    sellerMoney.charge(payment.getAmount());

	    userMoneyRepository.save(sellerMoney); // 신규일 경우 save 필요

	    // 4. 판매자 잔액 반환
	    return sellerMoney.getBalance();
	}
}