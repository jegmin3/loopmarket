package com.loopmarket.common.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.InputStream;
import java.io.IOException;

public class FirebaseConfig {

  public static void initializeFirebase() throws IOException {
    InputStream serviceAccount = FirebaseConfig.class
      .getClassLoader()
      .getResourceAsStream("config/firebase-service-key.json");

    FirebaseOptions options = FirebaseOptions.builder()
      .setCredentials(GoogleCredentials.fromStream(serviceAccount))
      .build();

    if (FirebaseApp.getApps().isEmpty()) {
      FirebaseApp.initializeApp(options);
    }
  }
}
