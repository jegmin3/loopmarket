package com.loopmarket.domain.admin.dashboard.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loopmarket.domain.admin.dashboard.Entity.LoginHistory;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    int countByLoginResultAndLoginTimeBetween(String loginResult, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT lh.userId) FROM LoginHistory lh WHERE lh.loginResult = 'SUCCESS' AND lh.loginTime BETWEEN :start AND :end")
    int countDistinctSuccessLoginsByDate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
}