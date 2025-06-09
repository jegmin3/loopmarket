package com.loopmarket.domain.pay.repository;

import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 거래 기록(money_transaction)에 접근하는 JPA 레포지토리
 */
public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Long> {

	// 거래 성사 건수: type in ('BUY_NOW', 'SAFE_PAY') and status = 'SUCCESS'
	long countByTypeInAndStatus(List<TransactionType> types, TransactionStatus status);
}