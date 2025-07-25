package com.loopmarket.domain.chat.controller;

import java.util.List;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpSession;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.chat.dto.ChatMessageDTO;
import com.loopmarket.domain.chat.dto.ChatMessageDTO.MessageType;
import com.loopmarket.domain.chat.dto.ChatRoomSummaryDTO;
import com.loopmarket.domain.chat.entity.ChatMessageEntity;
import com.loopmarket.domain.chat.entity.ChatRoomEntity;
import com.loopmarket.domain.chat.repository.ChatRoomRepository;
import com.loopmarket.domain.chat.service.ChatService;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController extends BaseController {
	
	private final ChatService chatService;
	private final SimpMessagingTemplate messagingTemplate;
	private final ProductRepository productRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ImageService imageService;
	
	/** 채팅기록에서 해당 채팅방 진입 */
	@GetMapping("/room/{roomId}")
	public String viewChatRoom(@PathVariable Long roomId,
	                           HttpSession session,
	                           Model model,
	                           RedirectAttributes redirectAttributes) {

	    // 로그인 사용자 정보
		MemberEntity loginUser = getLoginUser();
	    if (loginUser == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
	        return "redirect:/member/login";
	    }
	    Integer userId = loginUser.getUserId();

	    // 채팅방 조회
	    ChatRoomEntity room;
	    try {
	        room = chatService.getChatRoomById(roomId);
	    } catch (NoSuchElementException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 채팅방입니다.");
	        return "redirect:/chat/list";
	    }
	    // 본인이 나간 방이면 접근 불가 처리
	    if ((room.getUser1Id().equals(userId) && room.isUser1Leaved()) ||
	        (room.getUser2Id().equals(userId) && room.isUser2Leaved())) {
	        model.addAttribute("errorMessage", "이미 나간 채팅방입니다.");
	        return "redirect:/chat/list";
	    }

	    // 읽음 처리 + 업데이트된 메시지 반환
	    List<ChatMessageEntity> messageList = chatService.markMessagesAsRead(roomId, userId);
	    
	    // 내가 보낸 마지막 메시지 ID 추출
	    Long lastMyMsgId = null;
	    for (int i = messageList.size() - 1; i >= 0; i--) {
	        if (messageList.get(i).getSenderId().equals(userId)) {
	            lastMyMsgId = messageList.get(i).getMsgId();
	            break;
	        }
	    }
	    // 상품 정보 가져옴 (productId 타입 오류나서 findbyId(Long) 호출시 명시적으로 타입 변환함..)
	    ProductEntity product = productRepository.findById(room.getProductId().longValue()).orElseThrow();
	    // 상품 이미지 가져옴
	    String productImagePath = imageService.getThumbnailPath(product.getProductId());
	    model.addAttribute("productImagePath", productImagePath);

	    // 상대방 ID → 닉네임 조회
	    Integer opponentId = room.getUser1Id().equals(userId) ? room.getUser2Id() : room.getUser1Id();
	    String targetNickname = chatService.getNicknameByUserId(opponentId);

	    // 모델 데이터 전달
	    model.addAttribute("roomId", roomId);
	    model.addAttribute("messageList", messageList);
	    model.addAttribute("product", product);
	    model.addAttribute("targetNickname", targetNickname);
	    model.addAttribute("lastMyMsgId", lastMyMsgId);

	    return render("chat/chatRoom", model);
	}
	
	/**
	 * 상품 상세페이지에서 채팅하기 누를 시.
	 * 기존의 채팅방을 찾아 입장함.
	 * 없을시 상대방 ID만 전달. 채팅방 UI에서 roomId == null이면,
	 * 메시지 전송 전에 Ajax로 방 생성
	 */
	@GetMapping("/start")
	public String startChat(@RequestParam Integer targetId,
							@RequestParam Long productId,
	                        HttpSession session,
	                        Model model,
	                        RedirectAttributes redirectAttributes) {

	    // 로그인 사용자 ID 가져오기
	    MemberEntity loginUser = getLoginUser();
	    if (loginUser == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해주세요.");
	        return "redirect:/member/login";
	    }
	    
	    Integer userId = loginUser.getUserId();
	    
	    // 자기 자신과는 채팅 못함
	    if (userId.equals(targetId)) {
	        redirectAttributes.addFlashAttribute("errorMessage", "자기 자신과는 채팅할 수 없습니다.");
	        return "redirects:/products";
	    }

	    // 방이 존재하는지만 확인 (생성은 하지 않음, 없으면 null)
	    ChatRoomEntity existingRoom = chatService.findExistingRoom(userId, targetId, productId);
	    List<ChatMessageEntity> messages = existingRoom != null
	            ? chatService.getMessages(existingRoom.getRoomId())
	            : List.of();
	    
	    // 상대방 닉네임 가져오기
	    String targetNickname = chatService.getNicknameByUserId(targetId);
	    
	    // 상품 정보 추출
	    ProductEntity product = (existingRoom != null)
	            ? productRepository.findById(existingRoom.getProductId().longValue()).orElseThrow()
	            : productRepository.findById(productId.longValue()).orElseThrow();
	    // 상품 이미지 경로 가져오기
	    String productImagePath = imageService.getThumbnailPath(product.getProductId());
	    
	    // 뷰 데이터 설정
	    model.addAttribute("roomId", existingRoom != null ? existingRoom.getRoomId() : null);
	    model.addAttribute("messageList", messages);
	    model.addAttribute("targetNickname", targetNickname);
	    model.addAttribute("product", product);
	    model.addAttribute("productImagePath", productImagePath);
	    model.addAttribute("targetId", targetId); // JS에서 방 생성용으로 필요(ajax처리함)
	    
	    // 해당 채팅방 페이지로
	    return render("chat/chatRoom", model);
	}
	
	/** 채팅창 나가기 */
	@PostMapping("/leave")
	public String leaveRoom(@RequestParam Long roomId,
	                        HttpSession session,
	                        RedirectAttributes redirectAttributes) {
		
		MemberEntity loginUser = getLoginUser(); 
	    if (loginUser == null) {
	        redirectAttributes.addFlashAttribute("errorMessage", "세션이 만료되었습니다.");
	        return "redirect:/member/login";
	    }
	    Integer userId = loginUser.getUserId();

	    // 나가기 처리
	    chatService.leaveRoom(roomId, userId);
	    
	    // 상대방에게 알림 전송 (채팅방 정보 조회)
	    ChatRoomEntity room = chatRoomRepository.findById(roomId).orElse(null);
	    if (room == null) {
	        // 이미 삭제된 상태일 수 있음
	        redirectAttributes.addFlashAttribute("successMessage", "채팅방이 이미 종료되었습니다.");
	        return "redirect:/chat/list";
	    }
	    
	    ChatMessageDTO leaveNotice = new ChatMessageDTO();
	    leaveNotice.setType(MessageType.LEAVE);
	    leaveNotice.setRoomId(roomId);
	    leaveNotice.setSenderId(userId);
	    leaveNotice.setContent("상대가 채팅방을 나갔습니다. 지금부터 보내는 메시지는 전달되지 않습니다.");

	    // 유저별로 보내지 않음 (방 전체에 브로드캐스트)
	    messagingTemplate.convertAndSend("/queue/room." + roomId, leaveNotice);


	    redirectAttributes.addFlashAttribute("successMessage", "채팅방을 나갔습니다.");
	    return "redirect:/chat/list";
	}
	
	/** 채팅 목록으로  */
	@GetMapping("/list")
	public String chatList(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}
		Integer userId = loginUser.getUserId();
		
		List<ChatRoomSummaryDTO> summaries = chatService.getChatRoomSummaries(userId);
		
	    model.addAttribute("chatSummaries", summaries);

	    return render("chat/chatList", model);
	}

	

}
