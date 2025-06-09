package com.loopmarket.domain.admin.alram;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.loopmarket.domain.alram.AlramDTO;
import com.loopmarket.domain.alram.AlramEntity;
import com.loopmarket.domain.alram.AlramRepository;
import com.loopmarket.domain.alram.AlramService;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminAlramService {
	
	private final MemberRepository memberRepository;
	private final AlramService alramService;
	private final AlramRepository alramRepository;
	
    /**
     * 특정 사용자에게 관리자 알림 전송
     */
	public void sendAdminAlramToUser(AlramRequestDTO req) {
	    AlramDTO dto = AlramDTO.builder()
	        .userId(req.getUserId())
	        .senderId(null) // 시스템 or 관리자 발신자
	        .type("ADMIN")
	        .title(req.getTitle())
	        .content(req.getContent())
	        .build();
	    
	    alramService.createAdminAlram(dto);
	    System.out.println("특정 사용자에게 관리자 알림 전송됨: userId=" + req.getUserId());
	}
	
	/**
     * 전체 사용자에게 관리자 알림 전송
     */
	public void sendAdminAlramToAll(AlramRequestDTO req) {
	    List<MemberEntity> allUsers = memberRepository.findAll();
	    for (MemberEntity user : allUsers) {
	        AlramDTO dto = AlramDTO.builder()
	            .userId(user.getUserId())
	            .senderId(null)
	            .type("ADMIN")
	            .title(req.getTitle())
	            .content(req.getContent())
	            .build();
	        
	        alramService.createAdminAlram(dto);
	    }
	    System.out.println("전체 사용자에게 관리자 알림 전송 완료 (" + allUsers.size() + "명)");
	}
	
	/** 관리자 알림 조회 메서드 */
	public List<AlramDTO> getRecentAdminAlrams() {
	    List<AlramEntity> list = alramRepository.findTop30ByTypeOrderByCreatedAtDesc("ADMIN");

	    return list
	    		.stream()
	    		.map(entity -> AlramDTO.fromEntity(entity, memberRepository))
	    		.collect(Collectors.toList());
	}


}
