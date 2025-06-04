package com.loopmarket.domain.product.repository;

import com.loopmarket.domain.product.dto.CategoryProductStatsDTO;
import com.loopmarket.domain.product.entity.ProductEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * ProductEntity(상품) 정보를 데이터베이스에서 관리하는 저장소 인터페이스
 * JpaRepository를 상속하여 기본 CRUD(생성, 조회, 수정, 삭제) 기능을 제공
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
	long countByUserId(Long userId);

    List<ProductEntity> findByUserId(Long userId);

    List<ProductEntity> findByUserIdAndStatus(long userId, String status);

    // ✅ 거래중 상태들만 가져오기 위한 메서드 추가
    List<ProductEntity> findByUserIdAndStatusIn(Long userId, List<String> statuses);

    List<ProductEntity> findByCtgCode(Integer ctgCode);
    // 🔹 여러 소분류 코드에 해당하는 상품들 검색
    List<ProductEntity> findByCtgCodeIn(List<Integer> ctgCodes);

    List<ProductEntity> findByPriceBetween(Integer min, Integer max);
    List<ProductEntity> findByCtgCodeInAndPriceBetween(List<Integer> ctgCodes, Integer min, Integer max);
    List<ProductEntity> findByCtgCodeAndPriceBetween(Integer ctgCode, Integer min, Integer max);

     // 검색어가 제목이나 설명에 포함된 상품 조회
    List<ProductEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
    
    // 상품 등록 수 통계용-1
    @Query(value = 
    	    "SELECT " +
    	    "    DATE_FORMAT(created_at, '%Y-%u') AS week, " +
    	    "    COUNT(*) AS count " +
    	    "FROM products " +
    	    "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH) " +
    	    "GROUP BY week " +
    	    "ORDER BY week",
    	    nativeQuery = true)
    List<Object[]> countProductByWeekLastMonth();
    
    // 상품 등록 수 통계용-2
    int countByCreatedAtAfter(java.time.LocalDateTime dateTime);
    
    // 카테고리별 상품 통계용
    @Query(value = "SELECT p.ctg_code, c.ctg_name, COUNT(*) FROM products p JOIN category c ON p.ctg_code = c.ctg_code GROUP BY p.ctg_code, c.ctg_name", nativeQuery = true)
    List<Object[]> countProductsByCategory();

    // 숨김(false)이고 상태 ONSALE인 상품만 조회
    List<ProductEntity> findByIsHiddenFalseAndStatus(String status);

    // 숨김(false)이고 특정 카테고리인 상품만 조회
    List<ProductEntity> findByIsHiddenFalseAndCtgCode(Integer ctgCode);

    // 숨김(false)이고 소분류 코드 리스트 + 가격 범위 조건
    List<ProductEntity> findByIsHiddenFalseAndCtgCodeInAndPriceBetween(List<Integer> ctgCodes, Integer min, Integer max);

    // 숨김(false)이고 가격 범위 조건
    List<ProductEntity> findByIsHiddenFalseAndPriceBetween(Integer min, Integer max);
    
    List<ProductEntity> findByCtgCodeAndPriceBetweenAndIsHiddenFalse(Integer ctgCode, Integer min, Integer max);

    List<ProductEntity> findByIsHiddenFalseAndCtgCodeIn(List<Integer> ctgCodes);
    
}
