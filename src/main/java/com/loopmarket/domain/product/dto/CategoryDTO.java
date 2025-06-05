package com.loopmarket.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryDTO {
    private Integer ctgCode;
    private String ctgName;
}