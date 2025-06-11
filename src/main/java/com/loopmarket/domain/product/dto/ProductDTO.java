package com.loopmarket.domain.product.dto;

import lombok.Data;
import javax.validation.constraints.*;

@Data
public class ProductDTO {

  private Long productId;

  @NotBlank(message = "상품명을 입력해주세요.")
  private String title;

  @NotNull(message = "가격을 입력해주세요.")
  @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
  private Integer price;

  @NotNull(message = "카테고리를 선택해주세요.")
  private Integer ctgCode;

  @NotBlank(message = "판매 유형을 선택해주세요.")
  private String saleType;

  private Boolean isDirect;
  private Boolean isDelivery;
  private Boolean isNonface;

  private String condition;

  @NotNull(message = "상품 상태 점수를 입력해주세요.")
  private Integer conditionScore;

  @NotBlank(message = "상품 설명을 입력해주세요.")
  private String description;

  private String locationText;

  private Integer shippingFee;

  private Double latitude;
  private Double longitude;

  // 택배 체크 시 배송비 필수
  @AssertTrue(message = "택배 거래를 선택하셨다면 배송비를 입력하셔야 합니다.")
  public boolean isShippingFeeValid() {
    if (Boolean.TRUE.equals(isDelivery)) {
      return shippingFee != null && shippingFee > 0;
    }
    return true;
  }
  // 직거래 or 비대면 체크 시 위치 필수
  @AssertTrue(message = "직거래 또는 비대면을 선택하셨다면 거래 희망 장소를 입력하셔야 합니다.")
  public boolean isLocationTextValid() {
    if (Boolean.TRUE.equals(isDirect) || Boolean.TRUE.equals(isNonface)) {
      return locationText != null && !locationText.trim().isEmpty();
    }
    return true;
  }
}
