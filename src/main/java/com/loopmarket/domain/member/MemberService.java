package com.loopmarket.domain.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 이메일로 회원 조회
    @Transactional(readOnly = true)
    public MemberEntity findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElse(null);
    }

    // ID로 회원 조회 (기존 JpaRepository의 findById 사용 가능하지만 명시적 메서드)
    @Transactional(readOnly = true)
    public Optional<MemberEntity> findMemberById(Integer userId) {
        return memberRepository.findById(userId);
    }

    // FCM 토큰 업데이트
    @Transactional
    public void updateFcmToken(Integer userId, String fcmToken) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + userId));
        member.setFcmToken(fcmToken);
        memberRepository.save(member);
    }

    // TODO: (추가) 로그인 처리, 회원가입, 회원 정보 수정 등 다른 Member 관련 비즈니스 로직
}
