/* fcm 알림용 js */
// 공개키. 민감정보 아님
// Firebase 초기화 
const firebaseConfig = {
	apiKey: "AIzaSyDk2ENQ3XOA0wWoF_GdIP1cJOn9Hwx-z-Q",
	authDomain: "chatpractice-7d889.firebaseapp.com",
	projectId: "chatpractice-7d889",
	/*storageBucket: "chatpractice-7d889.firebasestorage.app",*/
	messagingSenderId: "414272984436",
	appId: "1:414272984436:web:a716a99114d79278dc4821"
};

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 익명 로그인 → 로그인 완료 후 getToken 요청
firebase.auth().signInAnonymously()
  .then(() => {
    console.log("익명 로그인 완료");
    return messaging.getToken({
      vapidKey: "BEJU92JxBLoO0j_oIT1jCNYorbb3JFmUuoz14vrsk52bcgX2nxgFhLwL6zWDizFi8_SLx5vgrXO8SkprLgQ_1dQ"
    });
  })
  .then(token => {
    console.log("FCM 토큰 발급 성공:", token);

    // 서버에 토큰 전달
    return fetch("/api/member/fcm-token", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token })
    });
  })
  .catch(err => {
    console.error("토큰 요청 실패:", err);
  });

// 실시간 알림 수신
messaging.onMessage((payload) => {
  const { title, body } = payload.notification;
  showAlert("info", title, body);
  $("#notifBadge").show().text("●");
});