package com.loopmarket.domain.admin.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.admin.dashboard.repository.LoginHistoryRepository;

@RestController
@RequestMapping("/admin/api/dashboard")
public class DashboardRestController {

    private final LoginHistoryRepository loginHistoryRepository;

    public DashboardRestController(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }
    
    @GetMapping("/today-login-count")
    public ResponseEntity<Map<String, Object>> getTodayLoginUserCount() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        int count = loginHistoryRepository.countByLoginResultAndLoginTimeBetween("SUCCESS", start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("todayLoginCount", count);

        return ResponseEntity.ok(response);
    }
}