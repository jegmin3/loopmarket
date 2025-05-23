package com.loopmarket.domain.image.entity;

import lombok.*;

import javax.persistence.*;

/**
 * 이미지 정보를 저장하는 엔티티 클래스
 * 이미지가 연결된 대상 테이블과 대상 ID를 함께 관리
 */
@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {

  /** 이미지 고유 ID (자동 생성) */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "img_id")
  private Long id;

  /** 이미지가 연결된 대상 테이블명 (예: "products") */
  @Column(name = "target_tbl", nullable = false)
  private String targetTable;

  /** 이미지가 연결된 대상 ID (예: 상품 ID) */
  @Column(name = "target_id", nullable = false)
  private Long targetId;

  /** 서버에 저장된 이미지 파일 경로(URL) */
  @Column(name = "img_path", nullable = false)
  private String imagePath;

  /** 대표 이미지 여부 (true: 대표 이미지) */
  @Column(name = "is_thumbnail")
  private Boolean isThumbnail;

  /** 이미지 순서(0부터 시작) */
  @Column(name = "img_seq")
  private Integer imgSeq;
}
