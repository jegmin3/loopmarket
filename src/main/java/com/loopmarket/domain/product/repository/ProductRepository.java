package com.loopmarket.domain.product.repository;

import com.loopmarket.domain.product.entity.ProductEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
	long countByUserId(Long userId);

    List<ProductEntity> findByUserId(Long userId);

    List<ProductEntity> findByUserIdAndStatus(long userId, String status);
    
    // ✅ 거래중 상태들만 가져오기 위한 메서드 추가
    List<ProductEntity> findByUserIdAndStatusIn(Long userId, List<String> statuses);
	
}

