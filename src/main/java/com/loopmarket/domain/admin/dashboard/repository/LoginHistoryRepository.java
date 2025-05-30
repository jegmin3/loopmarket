package com.loopmarket.domain.admin.dashboard.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopmarket.domain.admin.dashboard.Entity.LoginHistory;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    int countByLoginResultAndLoginTimeBetween(String loginResult, LocalDateTime start, LocalDateTime end);
}