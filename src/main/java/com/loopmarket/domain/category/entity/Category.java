package com.loopmarket.domain.category.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.loopmarket.domain.product.entity.ProductEntity;

@Entity
@Getter
@Setter
@Table(name = "category")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ctg_code")
  private Integer ctgCode;

  @Column(name = "ctg_name")
  private String ctgName;

  @Column(name = "up_ctg_code")
  private Integer upCtgCode;

  @Column(name = "ctg_show")
  private Integer ctgShow;

  private Integer seq;
  
  @OneToMany(mappedBy = "category")
  @JsonIgnore
  private List<ProductEntity> products;
}
