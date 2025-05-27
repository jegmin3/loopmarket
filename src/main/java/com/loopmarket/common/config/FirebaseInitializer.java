package com.loopmarket.common.config;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import com.loopmarket.common.config.FirebaseConfig;

@Component
public class FirebaseInitializer {

  @PostConstruct
  public void init() {
    try {
      FirebaseConfig.initializeFirebase(); // 방금 만든 Config 호출!
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
