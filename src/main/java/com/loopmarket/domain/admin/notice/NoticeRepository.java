package com.loopmarket.domain.admin.notice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Integer> {
    Page<NoticeEntity> findByPublishedTrue(Pageable pageable);
}
