package com.loopmarket.domain.alram;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlramRepository extends JpaRepository<AlramEntity, Long> {

    // 특정 사용자에 대한 전체 알림 조회 (최신순)
    List<AlramEntity> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // 읽지 않은 알림만 조회
    List<AlramEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
    
    /** 해당 유저가 가진 알림 중 채팅방 URL이 같은 게 있는지 조회하는 메서드 */
    Optional<AlramEntity> findByUserIdAndUrlAndType(Integer userId, String url, String type);
    
    /** 알림 타입이 "ADMIN"인 알림을 30개까지 최신순으로 가져오기 */
    List<AlramEntity> findTop30ByTypeOrderByCreatedAtDesc(String type);


}
