package com.loopmarket.admin.notice;

import com.loopmarket.admin.notice.dto.NoticeDTO;
import com.loopmarket.admin.notice.NoticeEntity;
import com.loopmarket.admin.notice.NoticeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/notice")
public class NoticeAdminController {

    private final NoticeRepository noticeRepository;

    @Autowired
    public NoticeAdminController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    // 공지사항 관리 페이지 진입
    @GetMapping
    public String noticeAdminPage(Model model) {
        List<NoticeEntity> noticeList = noticeRepository.findAll();
        model.addAttribute("notices", noticeList);
        return "admin/notice_admin :: content"; // AJAX로 fragment만 로드
    }

    // 공지사항 등록 폼
    @GetMapping("/form")
    public String showNoticeForm(Model model) {
        model.addAttribute("notice", new NoticeDTO()); // 빈 DTO 넘기기
        return "admin/form/notice_form :: content"; // fragment만 반환 (AJAX용)
    }

 // 공지사항 등록/수정 처리
    @PostMapping("/save")
    @ResponseBody
    public String saveNotice(@ModelAttribute NoticeDTO noticeDTO) {
        NoticeEntity notice;

        if (noticeDTO.getNoticeId() != null && noticeDTO.getNoticeId() > 0) {
            notice = noticeRepository.findById(noticeDTO.getNoticeId()).orElse(new NoticeEntity());
        } else {
            notice = new NoticeEntity();
        }

        notice.setTitle(noticeDTO.getTitle());
        notice.setContent(noticeDTO.getContent());
        notice.setPublished(noticeDTO.isPublished());

        noticeRepository.save(notice);
        return "success";
    }

    // 공지사항 수정 폼
    @GetMapping("/edit/{id}")
    public String editNotice(@PathVariable("id") int id, Model model) {
        NoticeEntity notice = noticeRepository.findById(id).orElse(null);
        if (notice == null) {
            return "redirect:/admin/notice";
        }
        model.addAttribute("notice", notice);
        return "admin/form/notice_form :: content";
    }

    // 공지사항 삭제
    @PostMapping("/delete/{id}")
    @ResponseBody
    public String deleteNotice(@PathVariable("id") int id) {
        try {
            noticeRepository.deleteById(id);
            return "success";  // 삭제 성공 메시지 반환
        } catch (Exception e) {
            return "error";    // 삭제 실패 시
        }
    }
}