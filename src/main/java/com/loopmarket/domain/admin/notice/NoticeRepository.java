package com.loopmarket.domain.admin.notice;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {

	List<NoticeEntity> findByPublishedTrueOrderByCreatedAtDesc();
	
}
