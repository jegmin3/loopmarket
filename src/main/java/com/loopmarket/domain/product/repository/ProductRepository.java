package com.loopmarket.domain.product.repository;

import com.loopmarket.domain.product.entity.ProductEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
