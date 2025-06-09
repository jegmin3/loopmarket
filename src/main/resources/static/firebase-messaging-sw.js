// firebase-messaging-sw.js
// fcm.js는 메인 브라우저에서 실행되는거고,
// 얘는 브라우저가 닫히거나 백그라운드에서도 알림을 전송하기 위함입니다.

// Firebase SDK 불러오기 (Service Worker 전용)
importScripts('https://www.gstatic.com/firebasejs/10.11.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.11.0/firebase-auth-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.11.0/firebase-messaging-compat.js');

// Firebase 구성
const firebaseConfig = {
  apiKey: "AIzaSyDk2ENQ3XOA0wWoF_GdIP1cJOn9Hwx-z-Q",
  authDomain: "chatpractice-7d889.firebaseapp.com",
  projectId: "chatpractice-7d889",
  storageBucket: "chatpractice-7d889.firebasestorage.app",
  messagingSenderId: "414272984436",
  appId: "1:414272984436:web:a716a99114d79278dc4821"
};

// Firebase 초기화
firebase.initializeApp(firebaseConfig);

// 백그라운드 푸시 메시지 수신 핸들러
const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
  console.log('[firebase-messaging-sw.js] 백그라운드 메시지 수신:', payload);

  const { title, body } = payload.notification;

  self.registration.showNotification(title, {
    body: body,
    icon: '/img/logo.png' // 푸시 알림에 사용할 아이콘 경로
  });
});
