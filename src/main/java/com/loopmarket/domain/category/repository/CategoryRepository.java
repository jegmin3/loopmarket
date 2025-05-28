package com.loopmarket.domain.category.repository;

import com.loopmarket.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

  // 대분류: 부모 코드 없는 것
  @Query("SELECT c FROM Category c WHERE c.upCtgCode IS NULL ORDER BY c.seq ASC")
  List<Category> findMainCategories();

  // 소분류: 특정 대분류의 하위 항목
  List<Category> findByUpCtgCodeOrderBySeqAsc(Integer upCtgCode);

  // 🔹 특정 대분류(upCtgCode)의 하위 소분류 코드 리스트만 뽑기
  @Query("SELECT c.ctgCode FROM Category c WHERE c.upCtgCode = :mainCode")
  List<Integer> findSubCategoryCodesByMainCode(Integer mainCode);


}
