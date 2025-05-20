package com.loopmarket.domain.pay.repository;

import com.loopmarket.domain.pay.entity.UserMoney;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 잔액 정보에 접근하기 위한 JPA 레포지토리
 */
public interface UserMoneyRepository extends JpaRepository<UserMoney, Long> {

}