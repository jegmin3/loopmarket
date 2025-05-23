package com.loopmarket.domain.location.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "location")
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long locationId;       // 위치 고유 ID (자동 생성)

  private String roadAddress;    // 도로명 주소
  private String jibunAddress;   // 지번 주소
  private String postalCode;     // 우편번호
  private String extraDetail;    // 상세 주소 (예: 건물명, 층수 등)
  private Double latitude;       // 위도 (GPS 좌표)
  private Double longitude;      // 경도 (GPS 좌표)

  private LocalDateTime createdAt;   // 등록일시
  private LocalDateTime updatedAt;   // 수정일시
}
