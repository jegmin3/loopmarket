package com.loopmarket.common.service;

import com.loopmarket.common.dto.ProductAiRequest;
import com.loopmarket.common.dto.ProductAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GptService {

  @Value("${openai.api.key}")
  private String apiKey;

  public ProductAiResponse generateFromImage(ProductAiRequest request) {
    String prompt =
      "아래 이미지는 전자기기 혹은 IT 관련 중고상품입니다.\n" +
        "1. 이 상품이 어떤 제품인지 추론해 간단한 상품명을 작성하세요." +
        "**'title'**은 간결하고 직관적으로, 가능하면 **브랜드명이나 모델명(애플, 에어팟 프로 등)**을 추측하여 포함해주세요.\\n\" +\n" +
        "\" 예: '에어팟 프로 2세대', '삼성 갤럭시 버즈 라이브\n" +
        "2. 다음 형식에 맞춰 상품 설명을 작성하세요. 말투는 깔끔하게, 리스트 형태로 쓰세요:\n\n" +
        "- 제품명: (예: 애플 에어팟 프로)\n" +
        "- 주요 기능 (2~4개 정도)\n" +
        "- 구성품 (있는 경우)\n" +
        "- 사용 상태 (예: 외관 깨끗, 정상 작동)\n" +
        "- 기타 특징 (예: 최신 모델, 생활 방수 지원 등)\n\n" +
        "3. 아래 보기 중 가장 적절한 소분류 카테고리명을 정확히 골라줘.\n" +
        "[스마트폰, 태블릿, 스마트워치, 휴대폰 액세서리, 노트북, 세탁기, 전자레인지, 청소기, 에어컨, DSLR, 미러리스, 액션캠, 캠코더, 렌즈, 이어폰, 헤드폰, 블루투스 스피커, 마이크, 오디오 인터페이스, 닌텐도 스위치, 플레이스테이션, 엑스박스, 게임 타이틀, 게임 컨트롤러, 라우터/공유기, 전자사전, 프로젝터, 외장하드, 웨어러블 기기]\n" +
        "응답은 반드시 JSON 형식으로 줘: { \"title\": \"...\", \"description\": \"...\", \"category\": \"소분류명\" }";

    RestTemplate restTemplate = new RestTemplate();
    String url = "https://api.openai.com/v1/chat/completions";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    // ✅ messages 배열 구성
    List<Map<String, Object>> contentList = new ArrayList<>();
    contentList.add(Map.of("type", "text", "text", prompt));

    // ✅ 여러 이미지 추가
    for (String imageUrl : request.getImageUrls()) {
      contentList.add(Map.of(
        "type", "image_url",
        "image_url", Map.of(
          "url", imageUrl,
          "detail", "auto"
        )
      ));
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-4o");
    requestBody.put("messages", List.of(
      Map.of("role", "user", "content", contentList)
    ));
    requestBody.put("max_tokens", 500);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

    // GPT 응답 파싱
    String content = (String) ((Map)((Map)((List) response.getBody().get("choices")).get(0)).get("message")).get("content");

    String title = content.split("\"title\":")[1].split("\"")[1];
    String description = content.split("\"description\":")[1].split("\"")[1];
    String categoryName = content.split("\"category\":")[1].split("\"")[1];

    Map<String, Integer> categoryMap = Map.ofEntries(
      Map.entry("스마트폰", 8),
      Map.entry("태블릿", 9),
      Map.entry("스마트워치", 10),
      Map.entry("휴대폰 액세서리", 11),
      Map.entry("노트북", 12),
      Map.entry("세탁기", 18),
      Map.entry("전자레인지", 19),
      Map.entry("청소기", 20),
      Map.entry("에어컨", 21),
      Map.entry("DSLR", 22),
      Map.entry("미러리스", 23),
      Map.entry("액션캠", 24),
      Map.entry("캠코더", 25),
      Map.entry("렌즈", 26),
      Map.entry("이어폰", 27),
      Map.entry("헤드폰", 28),
      Map.entry("블루투스 스피커", 29),
      Map.entry("마이크", 30),
      Map.entry("오디오 인터페이스", 31),
      Map.entry("닌텐도 스위치", 32),
      Map.entry("플레이스테이션", 33),
      Map.entry("엑스박스", 34),
      Map.entry("게임 타이틀", 35),
      Map.entry("게임 컨트롤러", 36),
      Map.entry("라우터/공유기", 37),
      Map.entry("전자사전", 38),
      Map.entry("프로젝터", 39),
      Map.entry("외장하드", 40),
      Map.entry("웨어러블 기기", 41)
    );

    int ctgCode = categoryMap.getOrDefault(categoryName, 8);

    return new ProductAiResponse(title, description, ctgCode);
  }

}
