package com.loopmarket.domain.admin.report;

import com.loopmarket.domain.report.dto.ReportDTO;
import com.loopmarket.domain.report.entity.ReportEntity;
import com.loopmarket.domain.report.repository.ReportRepository;
import com.loopmarket.domain.report.service.ReportService;
import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.product.repository.ProductRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController extends BaseController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ReportService reportService;
    
 // AJAX 요청 판단 유틸
    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    // AJAX 요청이면 fragment만, 아니면 전체 페이지 렌더링
    private String renderAdminReportPage(HttpServletRequest request, Model model, String viewName) {
        if (isAjaxRequest(request)) {
            return viewName + " :: content";  // Thymeleaf fragment 예: admin/report_admin.html 의 <div th:fragment="content">...</div>
        } else {
            return render(viewName, model);  // BaseController가 전체 레이아웃 렌더링 담당한다고 가정
        }
    }

    /**
     * 신고 목록 조회 - 페이징 포함
     */
    @GetMapping("")
    public String getReportList(HttpServletRequest request,
                                Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {

        Page<ReportEntity> reports = reportRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<ReportDTO> reportDTOList = reports.stream()
                                              .map(reportService::convertToDTO)
                                              .collect(Collectors.toList());

        Page<ReportDTO> reportDTOPage = new PageImpl<>(reportDTOList, reports.getPageable(), reports.getTotalElements());

        model.addAttribute("reports", reportDTOPage);

        return renderAdminReportPage(request, model, "admin/report_admin");
    }

    /**
     * 처리 상태 토글 (비동기 AJAX 요청 처리)
     */
    @PostMapping("/{id}/toggle")
    @ResponseBody
    public String toggleReportProcessed(@PathVariable Long id) {
        ReportEntity report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 정보를 찾을 수 없습니다."));

        report.setProcessed(!report.isProcessed());
        reportRepository.save(report);

        return "success";
    }

    /**
     * 신고 상세 폼 보기
     */
    @GetMapping("/{id}")
    public String showReportForm(HttpServletRequest request, @PathVariable Long id, Model model) {
        ReportDTO report = reportService.findById(id);
        model.addAttribute("report", report);
        return renderAdminReportPage(request, model, "admin/form/report_form");
    }

    /**
     * 신고 답변 등록 후 신고 목록 페이지로 리다이렉트
     */
//    @PostMapping("/{id}/reply")
//    @ResponseBody
//    public String submitReportReply(@PathVariable Long id,
//                                    @RequestParam String reply) {
//
//        try {
//            reportService.submitReply(id, reply);
//            return "success";
//        } catch (RuntimeException e) {
//            // 실패 시 메시지 반환하거나 상태 코드로 처리 가능
//            return "error: " + e.getMessage();
//        }
//    }
    
//    @PostMapping("/{id}/reply")
//    public String submitReportReply(@PathVariable Long id,
//                                    @RequestParam String reply,
//                                    Model model) {
//        try {
//            reportService.submitReply(id, reply);
//
//            Page<ReportEntity> reports = reportRepository.findAll(
//                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
//            );
//
//            List<ReportDTO> reportDTOList = reports.stream()
//                .map(reportService::convertToDTO)
//                .collect(Collectors.toList());
//
//            Page<ReportDTO> reportDTOPage = new PageImpl<>(reportDTOList, reports.getPageable(), reports.getTotalElements());
//            model.addAttribute("reports", reportDTOPage);
//
//            //return renderAdminReportPage(request, model, "admin/report_admin");
//            return "admin/report_reply :: reportReplyContent";
//        } catch (RuntimeException e) {
//            model.addAttribute("errorMessage", e.getMessage());
//            return "error_page"; // 에러용 뷰 페이지 이름
//        }
//    }
    
    @PostMapping("/{id}/reply")
    @ResponseBody
    public Map<String, Object> submitReply(@PathVariable Long id, @RequestParam String reply) {
        Map<String, Object> result = new HashMap<>();
        try {
            reportService.submitReply(id, reply);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "fail");
            result.put("message", e.getMessage());
        }
        return result;
    }
    
}