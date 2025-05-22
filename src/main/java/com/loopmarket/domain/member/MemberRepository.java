package com.loopmarket.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopmarket.domain.member.MemberEntity.Role;
import com.loopmarket.domain.member.MemberEntity.Status;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
	
	// findById() 등 기본적으로 제공되는 메서드는 없어도 됨
	
    Optional<MemberEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    
    // 닉네임으로 상요자 조회
    Optional<MemberEntity> findByNickname(String nickname);
    // 최근 가입자 / 최근 로그인 사용자 조회
    List<MemberEntity> findTop5ByOrderByCreatedAtDesc();
    List<MemberEntity> findTop5ByOrderByLastLoginAtDesc();
    
    // 상태 기준으로 사용자 찾기(관리자용)
    List<MemberEntity> findByStatus(Status status);
    // 관리자 계정 필터링
    List<MemberEntity> findByRole(Role role);

    
    // 소셜 로그인 대비
    // Optional<MemberEntity> findByEmailAndLoginType(String email, LoginType loginType);

}

