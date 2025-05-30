package com.loopmarket.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryProductStatsDTO {
	private Integer categoryCode;
	private String categoryName;
    private Integer count;
}
