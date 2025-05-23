package com.loopmarket.domain.image.repository;

import com.loopmarket.domain.image.entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 이미지 정보를 저장하고 조회하는 저장소 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 기능 사용
 */
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

  /**
   * 특정 상품 등 대상 ID에 연결된 대표 이미지(썸네일) 하나를 조회
   *
   * @param targetId 대상 ID (예: 상품 ID)
   * @param isThumbnail 대표 이미지 여부(true)
   * @return 대표 이미지 엔티티 또는 없으면 null
   */
  ImageEntity findFirstByTargetIdAndIsThumbnail(Long targetId, Boolean isThumbnail);

  /**
   * 특정 대상 테이블명과 ID에 연결된 모든 이미지 목록 조회
   *
   * @param targetTable 대상 테이블명 (예: "products")
   * @param targetId 대상 ID (예: 상품 ID)
   * @return 이미지 리스트
   */
  List<ImageEntity> findByTargetTableAndTargetId(String targetTable, Long targetId);
}
