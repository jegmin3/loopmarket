package com.loopmarket.domain.product.entity;

import lombok.Data;

import javax.persistence.*;

import com.loopmarket.domain.category.entity.Category;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productId;            // 상품 고유 ID

  private Long userId;               // 상품 등록자 사용자 ID
  private String title;              // 상품 제목
  private Integer price;             // 상품 가격

  @Column(name = "ctg_code")
  private Integer ctgCode;            // 카테고리 코드

  @ManyToOne
  @JoinColumn(name = "ctg_code", insertable = false, updatable = false)
  private Category category;
  
  @Column(name = "sale_type")
  private String saleType;           // 판매 유형 (예: 판매, 기부)

  private Boolean isDirect;          // 직접 거래 가능 여부
  private Boolean isDelivery;        // 택배 배송 가능 여부
  private Boolean isNonface;         // 비대면 거래 가능 여부


  private Long locationId;           // 거래 희망 위치 ID

  @Column(name = "`condition`", length = 30)
  private String condition;          // 상품 상태 (DB 예약어라 백틱 처리)

  @Column(name = "condition_score")
  private Integer conditionScore;    // 상태 점수 (슬라이더 값)

  @Column(length = 20)
  private String status;             // 상품 상태 상세 (짧은 설명)

  private String description;        // 상품 상세 설명
  private String locationText;       // 거래 희망 장소 텍스트
  private Integer shippingFee;       // 배송비

  private LocalDateTime createdAt;   // 등록일
  private LocalDateTime updateAt;    // 수정일
  private Boolean isHidden;          // 상품 숨김 여부

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;
  @Column(name = "view_count")
  private Integer viewCount;

  @Transient
  private boolean isBest;

  public boolean getIsBest() {
    return isBest;
  }

  public void setIsBest(boolean isBest) {
    this.isBest = isBest;
  }

  @Transient private String thumbnailPath;      // 대표 이미지 경로 (DB 저장 안됨)
  @Transient private List<String> imagePaths;   // 이미지 경로 리스트 (DB 저장 안됨)
  @Transient private String sellerNickname;
  @Transient private String relativeTime;
  @Transient private String dongName;

  @Transient private String sellerProfileImagePath;
  @Transient private LocalDateTime sellerJoinDate;
  @Transient private Integer sellerTotalSellCount;
  @Transient private Integer sellerTotalBuyCount;

  @Transient
  public String getDeliveryOnlyText() {
    if (Boolean.TRUE.equals(isDelivery) && !Boolean.TRUE.equals(isDirect) && !Boolean.TRUE.equals(isNonface)) {
      return "택배전용";
    }
    return null;
  }
  @Transient
  public boolean getDeliveryOnly() {
    return Boolean.TRUE.equals(isDelivery)
      && !Boolean.TRUE.equals(isDirect)
      && !Boolean.TRUE.equals(isNonface);
  }



}
