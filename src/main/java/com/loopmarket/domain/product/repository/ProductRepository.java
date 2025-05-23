package com.loopmarket.domain.product.repository;

import com.loopmarket.domain.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ProductEntity(상품) 정보를 데이터베이스에서 관리하는 저장소 인터페이스
 * JpaRepository를 상속하여 기본 CRUD(생성, 조회, 수정, 삭제) 기능을 제공
 */
@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
