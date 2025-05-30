package com.loopmarket.domain.member;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopmarket.domain.member.dto.WeeklyJoinStatsDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

    public boolean changePasswordByEmail(String email, String newPassword) {
        MemberEntity member = memberRepository.findByEmail(email)
        		.orElseThrow(() -> new IllegalArgumentException("Member not found with Email: " + email));
        if (member == null) {
            return false;
        }
        String hashed = passwordEncoder.encode(newPassword);
        member.setPassword(hashed);
        memberRepository.save(member);
        return true;
    }
    
    // 가입자 수 통계 뽑기-1
    public List<WeeklyJoinStatsDTO> getWeeklyJoinStats() {
        List<Object[]> result = memberRepository.countNewMembersByWeekLastMonth();

        return result.stream()
        	    .map((Object[] row) -> new WeeklyJoinStatsDTO((String) row[0], ((Number) row[1]).intValue()))
        	    .collect(Collectors.toList());
    }
    
    // 가입자 수 통계 뽑기-2
    public long getTotalMemberCount() {
        return memberRepository.count();
    }
    
    

    // TODO: (추가) 로그인 처리, 회원가입, 회원 정보 수정 등 다른 Member 관련 비즈니스 로직
}
