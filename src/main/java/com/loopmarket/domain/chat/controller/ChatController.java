package com.loopmarket.domain.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.chat.entity.ChatRoom;
import com.loopmarket.domain.chat.service.ChatService;
import com.loopmarket.domain.member.MemberEntity;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController extends BaseController {
	
	private final ChatService chatService;
	
	// 채팅방 목록
	@GetMapping
	public String chatList(Model model, RedirectAttributes redirectAttributes) {
		
	    MemberEntity loginUser = getLoginUser();
	    if (loginUser == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해 주세요.");
	        return "redirect:/member/login";
	    }
	    
	    Integer userId = getLoginUser().getUserId();
	    List<ChatRoom> chatRooms = chatService.getRoomsByUser(userId); // 회원아이디 기준 조회

	    model.addAttribute("chatRooms", chatRooms);
	    return render("chat/chatList", model);
	}

	// 1:1 채팅방
    @GetMapping("/{roomId}")
    public String chatRoom(@PathVariable String roomId,
                           @RequestParam("target") String targetUserId,
                           Model model) {
        model.addAttribute("roomId", roomId);
        model.addAttribute("targetUserId", targetUserId);
		
		return render("chat/chat", model);
	}
    
    // 채팅방 나가기
    @PostMapping("/exit")
    public String exitChat(@RequestParam Long roomId, RedirectAttributes redirectAttributes) {
        MemberEntity user = getLoginUser();
        //if (user == null) return "redirect:/member/login";

        chatService.exitRoom(roomId, user.getUserId());

        redirectAttributes.addFlashAttribute("successMessage", "채팅방에서 나갔습니다.");
        return "redirect:/chat"; // 목록으로 이동
    }

}
