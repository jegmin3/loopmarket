package com.loopmarket.domain.category.repository;

import com.loopmarket.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

  // 대분류만 가져오기 (up_ctg_code IS NULL)
  @Query("SELECT c FROM Category c WHERE c.upCtgCode IS NULL ORDER BY c.seq ASC")
  List<Category> findMainCategories();

}
