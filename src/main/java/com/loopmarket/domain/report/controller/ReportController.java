package com.loopmarket.domain.report.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.report.dto.ReportDTO;
import com.loopmarket.domain.report.service.ReportService;

import com.loopmarket.domain.member.MemberEntity;

@Controller
@RequestMapping("/report")
public class ReportController extends BaseController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // 신고 작성 폼 (GET)
    @GetMapping("/create")
    public String showReportForm(@RequestParam("productId") int productId,
                                 @RequestParam("reportedUserId") int reportedUserId,
                                 Model model,
                                 HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("productId", productId);
        model.addAttribute("reportedUserId", reportedUserId);

        return render("product/report", model);  // layout에 맞게 fragment 분리해서 처리
    }

    // 신고 제출 (POST)
    @PostMapping("/submit")
    public String submitReport(@ModelAttribute ReportDTO reportDTO,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // 로그인 여부 확인
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/login";
        }

        // MemberEntity로 캐스팅하여 신고자 ID 설정
        MemberEntity member = (MemberEntity) loginUser;
        reportDTO.setReporterId(member.getUserId());

        // 신고 저장
        reportService.saveReport(reportDTO);

        // 신고 완료 메시지 전달
        redirectAttributes.addFlashAttribute("msg", "신고가 정상 접수되었습니다.");

        // 마이페이지로 이동
        return "redirect:/mypage";
    }
}