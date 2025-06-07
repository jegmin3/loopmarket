/* fcm 알림용 js */

// 공개키. 민감정보 아님
// Firebase 초기화 
const firebaseConfig = {
	apiKey: "AIzaSyDk2ENQ3XOA0wWoF_GdIP1cJOn9Hwx-z-Q",
	authDomain: "chatpractice-7d889.firebaseapp.com",
	projectId: "chatpractice-7d889",
	storageBucket: "chatpractice-7d889.firebasestorage.app",
	messagingSenderId: "414272984436",
	appId: "1:414272984436:web:a716a99114d79278dc4821"
};

/* 서비스워커 (firebase-messaging-sw.js) 등록*/
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/firebase-messaging-sw.js')
    .then(function (registration) {
      //console.log("Service Worker 등록 성공:", registration.scope);
      //messaging.useServiceWorker(registration); // 등록된 SW를 명시적으로 사용
    }).catch(function (err) {
      console.error("Service Worker 등록 실패:", err);
    });
}

firebase.initializeApp(firebaseConfig);

const messaging = firebase.messaging();

// 익명 로그인 → 로그인 완료 후 getToken 요청
firebase.auth().onAuthStateChanged((user) => {
  if (user) {
    console.log("기존 익명 사용자 로그인 유지:", user.uid);
    getAndSendToken();
  } else {
    firebase.auth().signInAnonymously()
      .then(() => {
        console.log("새 익명 로그인 완료");
        getAndSendToken();
      })
      .catch((err) => {
        console.error("익명 로그인 실패:", err);
      });
  }
});

function getAndSendToken() {
  messaging.getToken({ vapidKey: "BEJU92JxBLoO0j_oIT1jCNYorbb3JFmUuoz14vrsk52bcgX2nxgFhLwL6zWDizFi8_SLx5vgrXO8SkprLgQ_1dQ" })
    .then((token) => {
      //console.log("FCM 토큰:", token);
      return fetch("/api/member/fcm-token", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ token })
      });
    })
    .catch((err) => {
      console.error("토큰 요청 실패:", err);
    });
}

// 실시간 알림 수신
//console.log("onMessage 리스너 시작됨");
setTimeout(() => {
	messaging.onMessage((payload) => {
	  console.log("FCM 수신됨:", payload);
	  //const title = payload.data.title;
	  //const body = payload.data.body;
	  //showAlert("info", title, body);
	  $("#notifBadge").show().text("●");
	});
}, 500); // 0.5초 뒤에 리스너 등록