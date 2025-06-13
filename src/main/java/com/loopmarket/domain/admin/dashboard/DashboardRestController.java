package com.loopmarket.domain.admin.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;
import com.loopmarket.domain.admin.dashboard.repository.LoginHistoryRepository;
import com.loopmarket.domain.pay.repository.MoneyTransactionRepository;
import com.loopmarket.domain.product.repository.ProductRepository;

@RestController
@RequestMapping("/admin/api/dashboard")
public class DashboardRestController {

    private final LoginHistoryRepository loginHistoryRepository;
    private final ProductRepository productRepository;
    private final MoneyTransactionRepository moneyTransactionRepository;

    public DashboardRestController(LoginHistoryRepository loginHistoryRepository,ProductRepository productRepository,MoneyTransactionRepository moneyTransactionRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.productRepository =  productRepository;
        this.moneyTransactionRepository = moneyTransactionRepository;
    }
    
//    private int countSuccessfulLoginsByDate(LocalDate date) {
//        LocalDateTime start = date.atStartOfDay(); // 00:00
//        LocalDateTime end = date.plusDays(1).atStartOfDay(); // 다음날 00:00
//        return loginHistoryRepository.countByLoginResultAndLoginTimeBetween("SUCCESS", start, end);
//    }
    
    private int countSuccessfulLoginsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return loginHistoryRepository.countDistinctSuccessLoginsByDate(start, end);
    }
    
    @GetMapping("/today-login-count")
    public ResponseEntity<Map<String, Object>> getTodayLoginUserCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        int count = loginHistoryRepository.countDistinctSuccessLoginsByDate(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("todayLoginCount", count);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/weekly-login-stats")
    @ResponseBody
    public Map<String, Object> getWeeklyLoginStats() {
        Map<String, Object> response = new HashMap<>();

        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.format(formatter));

            // 로그인 수 조회 (로그인 성공만 기준)
            int loginCount = countSuccessfulLoginsByDate(date);
            counts.add(loginCount);
        }

        response.put("labels", labels);
        response.put("counts", counts);
        return response;
    }
    
    @GetMapping("/trade-stats")
    public Map<String, Object> getTradeStats() {
        List<TransactionType> completedTypes = List.of(TransactionType.BUY_NOW, TransactionType.SAFE_PAY);
        Map<String, Object> result = new HashMap<>();

        // 전체 성사 건수
        int completedCount = (int) moneyTransactionRepository.countByTypeInAndStatus(completedTypes, TransactionStatus.SUCCESS);
        long totalProducts = productRepository.count(); // 전체 상품 수 기준

        int uncompletedCount = (int)(totalProducts - completedCount);
        if (uncompletedCount < 0) uncompletedCount = 0;

        result.put("성사", completedCount);
        result.put("미성사", uncompletedCount);

        // 오늘 날짜 기준
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        int todayCompleted = moneyTransactionRepository.countByTypeInAndStatusAndCreatedAtBetween(
            completedTypes,
            TransactionStatus.SUCCESS,
            startOfDay,
            endOfDay
        );

        int todayTotalAttempts = moneyTransactionRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        double todaySuccessRate = todayTotalAttempts > 0
            ? (todayCompleted * 100.0 / todayTotalAttempts)
            : 0.0;

        result.put("오늘성사건수", todayCompleted);
        result.put("오늘총거래시도", todayTotalAttempts);
        result.put("오늘성사율", Math.round(todaySuccessRate * 10) / 10.0); // 소수점 1자리

        // 최근 30일 성사 건수
        LocalDate thirtyDaysAgo = today.minusDays(29); // 오늘 포함 30일
        List<Object[]> dailyStats = moneyTransactionRepository.countDailyCompletedTransactions(
            completedTypes,
            TransactionStatus.SUCCESS,
            thirtyDaysAgo.atStartOfDay(),
            endOfDay
        );

        Map<String, Integer> dailyCompletedMap = new TreeMap<>();
        for (Object[] row : dailyStats) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];  // 먼저 sql.Date로 캐스팅
            LocalDate date = sqlDate.toLocalDate();          // 변환
            Long count = (Long) row[1];
            dailyCompletedMap.put(date.toString(), count.intValue());
        }

        result.put("최근30일일별성사", dailyCompletedMap);

        return result;
    }
    
}