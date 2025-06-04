package com.loopmarket.domain.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductAdminListDTO {
    private Long productId;
    private String title;
    private Integer price;
    private Integer ctgCode;
    private String saleType;
    private String condition;
    private String status;
    
    @JsonProperty("ishidden")
    private Boolean isHidden;
}