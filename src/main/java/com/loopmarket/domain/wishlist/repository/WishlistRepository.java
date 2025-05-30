package com.loopmarket.domain.wishlist.repository;

import com.loopmarket.domain.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 관심목록(Wishlist) 레포지토리
 * 
 * 사용자 찜 목록 데이터를 처리하는 JPA 인터페이스입니다.
 */
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 특정 사용자가 특정 상품을 찜했는지 확인
    Optional<Wishlist> findByUserIdAndProdId(Long userId, Long prodId);

    // 찜 해제를 위한 삭제 메서드
    void deleteByUserIdAndProdId(Long userId, Long prodId);
}