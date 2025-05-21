package com.loopmarket.domain.location.service;

import com.loopmarket.domain.location.entity.Location;
import com.loopmarket.domain.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // ✅ 이거 추가!

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

  private final LocationRepository locationRepository;

  @Override
  public List<String> getRecommendedDongNames() {
    List<Location> locations = locationRepository.findTop6ByRecent();

    return locations.stream()
      .map(location -> {
        String jibun = location.getJibunAddress(); // 예: 해운대구 우동 1500
        if (jibun != null) {
          String[] parts = jibun.trim().split(" ");
          if (parts.length >= 2) {
            // 부산광역시 + 구 + 동 형태로 리턴
            return "부산광역시 " + parts[0] + " " + parts[1]; // parts[0]: 구, parts[1]: 동
          }
        }
        return "지역정보없음";
      })
      .collect(Collectors.toList());
  }




}
