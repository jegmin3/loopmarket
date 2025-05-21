package com.loopmarket.domain.location.repository;

import com.loopmarket.domain.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

  // 추천 지역 6개만 가져오기
  @Query(value = "SELECT * FROM location ORDER BY created_at DESC LIMIT 6", nativeQuery = true)
  List<Location> findTop6ByRecent();
}
