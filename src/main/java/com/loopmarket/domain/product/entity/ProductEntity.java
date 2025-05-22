package com.loopmarket.domain.product.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Data
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productId;

  private Long userId;
  private String title;
  private Integer price;
  private String ctgCode;
  private String saleType;

  private Boolean isDirect;
  private Boolean isDelivery;
  private Boolean isNonface;

  private Long locationId;
  private String condition;
  private String status;

  private String description;
  private String locationText;    // 💡 거래 희망 장소
  private Integer shippingFee;    // 💡 택배비

  private LocalDateTime createdAt;
  private LocalDateTime updateAt;
  private Boolean isHidden;
}

