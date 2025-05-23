package com.loopmarket.domain.image.service;

import com.loopmarket.domain.image.entity.ImageEntity;
import com.loopmarket.domain.image.repository.ImageRepository;
import com.loopmarket.common.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final ImageRepository imageRepository;         // 이미지 엔티티 DB 접근용 리포지토리
  private final FileStorageService fileStorageService;   // 실제 파일 저장 처리 서비스

  /**
   * 상품에 연결된 이미지들을 저장 처리한다.
   *
   * @param productId 저장 대상 상품 ID
   * @param images     업로드된 이미지 파일 리스트
   * @param mainImageIndex 대표 이미지로 지정할 인덱스
   */
  public void saveImagesForProduct(Long productId, List<MultipartFile> images, int mainImageIndex) {
    for (int i = 0; i < images.size(); i++) {
      MultipartFile file = images.get(i);

      // 실제 서버에 파일 저장 후 저장된 경로(파일 URL) 받기
      String savedPath = fileStorageService.save(file);

      // 이미지 엔티티 생성
      ImageEntity image = ImageEntity.builder()
        .targetTable("products")             // 어떤 테이블의 데이터와 연관되는지 명시 (여기선 products)
        .targetId(productId)                 // 연관된 상품 ID
        .imagePath(savedPath)                // 저장된 이미지 경로
        .isThumbnail(i == mainImageIndex)   // 대표 이미지 여부 판단
        .imgSeq(i)                          // 이미지 순서(0부터 시작)
        .build();

      // DB에 이미지 정보 저장
      imageRepository.save(image);
    }
  }

  /**
   * 특정 상품에 연결된 모든 이미지 경로 리스트를 조회한다.
   *
   * @param productId 조회 대상 상품 ID
   * @return 이미지 경로 문자열 리스트
   */
  public List<String> getAllImagePaths(Long productId) {
    List<ImageEntity> images = imageRepository.findByTargetTableAndTargetId("products", productId);
    return images.stream()
      .map(ImageEntity::getImagePath)
      .collect(Collectors.toList());
  }

  /**
   * 특정 상품의 대표 이미지(썸네일) 경로를 조회한다.
   * 대표 이미지가 없으면 기본 이미지 경로를 반환한다.
   *
   * @param productId 조회 대상 상품 ID
   * @return 대표 이미지 경로 또는 기본 이미지 경로
   */
  public String getThumbnailPath(Long productId) {
    ImageEntity thumbnail = imageRepository.findFirstByTargetIdAndIsThumbnail(productId, true);

    // 썸네일 이미지 없으면 기본 이미지 경로 반환
    if (thumbnail == null) {
      return "/img.pay/kakao.png"; // 기본 이미지 파일은 static/img.pay/kakao.png 위치에 있어야 함
    }

    // 대표 이미지 경로 반환
    return thumbnail.getImagePath();
  }

}
