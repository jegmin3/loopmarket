package com.loopmarket.domain.chat.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.loopmarket.common.controller.GetMemberInfo;
import com.loopmarket.common.service.S3Service;
import com.loopmarket.domain.chat.dto.UnreadChatDTO;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.service.ChatService;
import com.loopmarket.domain.member.MemberEntity;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatApiController extends GetMemberInfo {
	
	private final ChatService chatService;
	private final S3Service s3Service;
	
	/** 채팅하기 누르고 입장 후, 메시지를 입력했을때 방이 만들어지게 매핑된 메서드 */
	@PostMapping("/create-room")
	@ResponseBody
	public Map<String, Object> createRoom(@RequestParam Integer targetId, @RequestParam Long productId, HttpSession session) {
		MemberEntity loginUser = getLoginUser();
	    Integer userId = loginUser.getUserId();

	    ChatRoomEntity room = chatService.enterRoom(userId, targetId, productId); // 여기선 생성 허용
	    return Map.of("roomId", room.getRoomId());
	}
	
	// 안읽은 메시지 반환
    @GetMapping("/unread-summary")
    @ResponseBody
    public List<UnreadChatDTO> getUnreadSummary(HttpSession session) {
		MemberEntity loginUser = getLoginUser();
	    Integer userId = loginUser.getUserId();
	    
        return chatService.getUnreadSummariesWithLastMessage(userId);
    }
	
    /**
     * 채팅 이미지 업로드 처리용 API
     *
     * 클라이언트로부터 전송된 MultipartFile을 받아 AWS S3에 업로드하고
     * 해당 파일의 접근 가능한 외부 URL을 반환함.
     *
     * 사용 예시 (JS):
     *   formData.append("file", imageFile);
     *   POST /api/chat/upload
     *
     * 반환값 예시:
     *   {
     *     "imageUrl": "https://s3-bucket-url/chat/uuid_파일명.jpg"
     *   }
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadChatImage(@RequestParam MultipartFile file) {
        try {
            // 파일을 S3에 업로드하고 접근 가능한 URL을 가져옴
            String imageUrl = s3Service.uploadFile(file, "chat");

            // 클라이언트에 이미지 URL 반환
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            // 업로드 중 오류가 발생한 경우 로그 출력
            e.printStackTrace();

            // 500 상태코드와 함께 에러 메시지 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("error", "업로드 중 오류 발생"));
        }
    }

	
	

}
