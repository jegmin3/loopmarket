package com.loopmarket.domain.pay.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "user_money")
@Getter

/**
 * JPA가 리플렉션으로 객체 생성을 하기 위한 기본 생성자
 * PROTECTED로 외부 생성은 제한
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMoney {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private int balance;

    public UserMoney(Long userId, int balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public void charge(int amount) {
        this.balance += amount;
    }

    public void refund(int amount) {
        if (this.balance < amount) 
        	//생성자에 "잔액이 부족합니다." 문자열을 전달 -> 나중에 e.getMessage()로 꺼낼 수 있음
        	throw new IllegalArgumentException("잔액이 부족합니다.");
        this.balance -= amount;
    }
}