package com.loopmarket.domain.alram;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlramRepository extends JpaRepository<AlramEntity, Integer> {

    List<AlramEntity> findByUserIdAndReadFalseOrderByCreatedAtDesc(Integer userId);
}

