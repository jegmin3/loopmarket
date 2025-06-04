package com.loopmarket.domain.category.repository;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.entity.CategoryWithCountDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  List<Category> findAllByOrderBySeqAsc();
  
  @Query("SELECT new com.loopmarket.domain.category.entity.CategoryWithCountDTO(c.ctgCode, c.ctgName, c.upCtgCode, COUNT(p)) " +
	       "FROM Category c LEFT JOIN c.products p " + 
	       "GROUP BY c.ctgCode, c.ctgName, c.upCtgCode " +
	       "ORDER BY c.seq ASC")
	List<CategoryWithCountDTO> findCategoriesWithProductCount();

  @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.ctgCode = :ctgCode")
  int countProductsInCategory(@Param("ctgCode") Integer ctgCode);

  @Query("SELECT COALESCE(MAX(c.seq), 0) FROM Category c")
  int getMaxSeq();
  

}
