package com.loopmarket.domain.category.service;

import com.loopmarket.domain.category.entity.Category;
import com.loopmarket.domain.category.entity.CategoryWithCountDTO;
import com.loopmarket.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  // 대분류 카테고리 리스트 조회
  public List<Category> getMainCategories() {
    return categoryRepository.findMainCategories();
  }

  // 특정 대분류의 소분류 리스트 조회
  public List<Category> getSubCategories(Integer mainCategoryCode) {
    return categoryRepository.findByUpCtgCodeOrderBySeqAsc(mainCategoryCode);
  }

  // 특정 대분류의 소분류 코드 리스트만 조회
  public List<Integer> getSubCategoryCodes(Integer mainCategoryCode) {
    return categoryRepository.findSubCategoryCodesByMainCode(mainCategoryCode);
  }

  // 단일 카테고리 조회
  public Category getCategoryById(Integer ctgCode) {
    return categoryRepository.findById(ctgCode)
      .orElseThrow(() -> new NoSuchElementException("카테고리를 찾을 수 없습니다: " + ctgCode));
  }

  // 카테고리 저장 (신규 or 수정)
  public Category saveCategory(Category category) {
    return categoryRepository.save(category);
  }

  // 카테고리 삭제
  public void deleteCategory(Integer ctgCode) {
    categoryRepository.deleteById(ctgCode);
  }

  public List<Category> findAllCategories() {
      return categoryRepository.findAllByOrderBySeqAsc();
  }
  
  // 카테고리 + 상품 수
  public List<CategoryWithCountDTO> findCategoriesWithProductCount() {
	  return categoryRepository.findCategoriesWithProductCount();
  }
  // 카테고리 추가
  public void addCategory(String name, Integer upCtgCode) {
	    Category category = new Category();
	    category.setCtgName(name);
	    category.setUpCtgCode(upCtgCode);  // null이면 상위 카테고리로 저장됨
	    categoryRepository.save(category);
	}
  // 상품 없을 때만 삭제
  public boolean deleteCategoryIfNoProducts(int ctgCode) {
	  int productCount = categoryRepository.countProductsInCategory(ctgCode);
	  if (productCount == 0) {
	      categoryRepository.deleteById(ctgCode);
	      return true;
	  } else {
	      return false;
	  }
  }


}
  
