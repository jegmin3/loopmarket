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

import com.loopmarket.domain.admin.dashboard.repository.LoginHistoryRepository;

@RestController
@RequestMapping("/admin/api/dashboard")
public class DashboardRestController {

    private final LoginHistoryRepository loginHistoryRepository;

    public DashboardRestController(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }
    
    private int countSuccessfulLoginsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay(); // 00:00
        LocalDateTime end = date.plusDays(1).atStartOfDay(); // 다음날 00:00
        return loginHistoryRepository.countByLoginResultAndLoginTimeBetween("SUCCESS", start, end);
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
}