package com.loopmarket.domain.pay.repository;

import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 사용자 거래 기록(money_transaction)에 접근하는 JPA 레포지토리
 */
public interface MoneyTransactionRepository extends JpaRepository<MoneyTransaction, Long> {

    // 전체 성사 건수
    long countByTypeInAndStatus(List<TransactionType> types, TransactionStatus status);

    // 오늘 성사 건수
    int countByTypeInAndStatusAndCreatedAtBetween(
        List<TransactionType> types,
        TransactionStatus status,
        LocalDateTime start,
        LocalDateTime end
    );

    // 오늘 전체 거래 시도 (상태 무관)
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // 최근 30일 일자별 성사 건수
    @Query("SELECT DATE(mt.createdAt), COUNT(mt) " +
           "FROM MoneyTransaction mt " +
           "WHERE mt.type IN :types AND mt.status = :status AND mt.createdAt BETWEEN :start AND :end " +
           "GROUP BY DATE(mt.createdAt) ORDER BY DATE(mt.createdAt)")
    List<Object[]> countDailyCompletedTransactions(
        @Param("types") List<TransactionType> types,
        @Param("status") TransactionStatus status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}