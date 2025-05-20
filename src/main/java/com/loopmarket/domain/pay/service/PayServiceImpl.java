package com.loopmarket.domain.pay.service;

import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.entity.UserMoney;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;
import com.loopmarket.domain.pay.repository.MoneyTransactionRepository;
import com.loopmarket.domain.pay.repository.UserMoneyRepository;
import lombok.RequiredArgsConstructor;
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
        // 1. 사용자 잔액 조회 또는 신규 생성
        UserMoney userMoney = userMoneyRepository.findById(userId)
                .orElse(new UserMoney(userId, 0));

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
}