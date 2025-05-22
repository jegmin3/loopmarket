package com.loopmarket.notice;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.loopmarket.admin.notice.NoticeEntity;
import com.loopmarket.admin.notice.NoticeRepository;
import com.loopmarket.common.controller.BaseController;
import com.loopmarket.common.controller.LayoutController;

@Controller
@RequestMapping("/notice")
public class NoticeController extends BaseController{

    private final NoticeRepository noticeRepository;

    public NoticeController(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    // 공지사항 목록 (유저용)
    @GetMapping
    public String getNoticeList(Model model) {
        List<NoticeEntity> publishedNotices = noticeRepository.findByPublishedTrueOrderByCreatedAtDesc();
        model.addAttribute("notices", publishedNotices);
        return render("notice/notice",model); // resources/templates/notice.html 렌더링
    }

    // 공지 상세 보기
    @GetMapping("/view/{id}")
    public String getNoticeDetail(@PathVariable("id") int id, Model model) {
        NoticeEntity notice = noticeRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("notice", notice);
        return render("notice/notice_view",model); // 상세 페이지 (새로 만드셔야 함)
    }
}