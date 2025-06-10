package com.loopmarket.domain.admin.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, Integer> getTradeStats() {
        List<TransactionType> completedTypes = List.of(TransactionType.BUY_NOW, TransactionType.SAFE_PAY);
        
        int completedCount = (int) moneyTransactionRepository.countByTypeInAndStatus(completedTypes, TransactionStatus.SUCCESS);
        long totalProducts = productRepository.count();

        int uncompletedCount = (int)(totalProducts - completedCount);
        if (uncompletedCount < 0) uncompletedCount = 0;

        Map<String, Integer> result = new HashMap<>();
        result.put("성사", completedCount);
        result.put("미성사", uncompletedCount);

        return result;
    }
    
}