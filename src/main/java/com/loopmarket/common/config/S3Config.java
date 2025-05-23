package com.loopmarket.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 클라이언트를 설정하는 Configuration 클래스
 */
@Configuration
public class S3Config {

  /**
   * AWS Access Key (application.yml 또는 환경 변수에 설정)
   */
  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  /**
   * AWS Secret Key (application.yml 또는 환경 변수에 설정)
   */
  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  /**
   * AWS 리전 정보 (application.yml 또는 환경 변수에 설정)
   */
  @Value("${cloud.aws.region.static}")
  private String region;

  /**
   * S3Client 빈 생성 메서드
   *
   * @return 초기화된 S3Client 객체
   */
  @Bean
  public S3Client s3Client() {
    // AWS 자격 증명 생성
    AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

    // S3Client 빌더를 사용해 리전과 자격 증명을 설정하고 빌드
    return S3Client.builder()
      .region(Region.of(region))
      .credentialsProvider(StaticCredentialsProvider.create(credentials))
      .build();
  }
}
