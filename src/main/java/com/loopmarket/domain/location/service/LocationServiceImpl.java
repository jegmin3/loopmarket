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

  private final LocationRepository locationRepository; // 위치 정보 DB 접근 리포지토리

  /**
   * 최근 등록된 위치 6개를 조회해서
   * 부산광역시 + 구 + 동 이름 형태의 리스트로 반환한다.
   *
   * @return 추천 동 이름 리스트 (예: "부산광역시 해운대구 우동")
   */
  @Override
  public List<String> getRecommendedDongNames() {
    // 최근 위치 6개를 DB에서 조회
    List<Location> locations = locationRepository.findTop6ByRecent();

    // 조회된 위치 리스트를 구/동 이름 형태 문자열 리스트로 변환
    return locations.stream()
      .map(location -> {
        String jibun = location.getJibunAddress(); // 예: "해운대구 우동 1500"
        if (jibun != null) {
          String[] parts = jibun.trim().split(" ");
          if (parts.length >= 2) {
            // 부산광역시 + 구 + 동 이름 조합해서 반환
            return "부산광역시 " + parts[0] + " " + parts[1]; // parts[0]: 구, parts[1]: 동
          }
        }
        return "지역정보없음"; // 주소 정보 없으면 기본 문자열 반환
      })
      .collect(Collectors.toList());
  }
}
