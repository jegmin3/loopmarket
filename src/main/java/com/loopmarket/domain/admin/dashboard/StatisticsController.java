package com.loopmarket.domain.admin.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.member.MemberService;
import com.loopmarket.domain.member.dto.WeeklyJoinStatsDTO;
import com.loopmarket.domain.product.dto.CategoryProductStatsDTO;
import com.loopmarket.domain.product.dto.WeeklyProductStatsDTO;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/statistics")
public class StatisticsController {

    private final MemberService memberService;
    private final ProductService productService;

    @GetMapping("/weekly-join")
    public Map<String, Object> getWeeklyJoinStats() {
        List<WeeklyJoinStatsDTO> weeklyStats = memberService.getWeeklyJoinStats();
        long totalCount = memberService.getTotalMemberCount();

        return Map.of(
            "weeklyStats", weeklyStats,
            "totalCount", totalCount
        );
    }
    
    @GetMapping("/weekly-product")
    public Map<String, Object> getWeeklyItemStats() {
        List<WeeklyProductStatsDTO> weeklyStats = productService.getWeeklyProductStats();
        return Map.of("weeklyStats", weeklyStats);
    }
    
    @GetMapping("/total-products")
    public Map<String, Object> getTotalProductsLastMonth() {
        int totalCount = productService.countTotalProductsLastMonth();
        Map<String, Object> response = new HashMap<>();
        response.put("totalProductCount", totalCount);
        return response;
    }
    
    @GetMapping("/category-product-stats")
    public ResponseEntity<List<CategoryProductStatsDTO>> getCategoryProductStats() {
        return ResponseEntity.ok(productService.getCategoryProductStats());
    }
    
}