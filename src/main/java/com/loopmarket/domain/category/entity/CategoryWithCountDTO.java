package com.loopmarket.domain.category.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryWithCountDTO {
	private int ctgCode;
    private String ctgName;
    private Integer upCtgCode;       // 상위 카테고리 코드 추가
    private long productCount;
}
