package com.loopmarket.domain.alram;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlramRepository extends JpaRepository<AlramEntity, Long> {

    // 특정 사용자에 대한 전체 알림 조회 (최신순)
    List<AlramEntity> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // 읽지 않은 알림만 조회
    List<AlramEntity> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId);
}
