package com.loopmarket.domain.purchase.repository;

import com.loopmarket.domain.purchase.entity.PurchaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PurchaseRepository extends JpaRepository<PurchaseEntity, Long> {
	long countByBuyerId(Long buyerId);
}