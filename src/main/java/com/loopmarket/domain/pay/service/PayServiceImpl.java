package com.loopmarket.domain.pay.service;

import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.entity.UserMoney;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;
import com.loopmarket.domain.pay.repository.MoneyTransactionRepository;
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
		int currentBalance = userMoney.getBalance();
		if (currentBalance + amount > 10_000_000) {
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
	 */
	@Override
	@Transactional(readOnly = true)
	public int getCurrentBalance(Long userId) {
		return userMoneyRepository.findById(userId).map(UserMoney::getBalance).orElse(0);
	}
}