let stompClient = null;
const roomId = window.roomId;       // layout에서 설정
const senderId = window.senderId;
// WebSocket 연결
let reconnectCount = 0;

// 웹소켓 연결 함수
function connect() {
    const socket = new SockJS("/ws/chat");
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("✅ WebSocket 연결됨");

        stompClient.subscribe(`/queue/room.${roomId}`, function (message) {
            showMessage(JSON.parse(message.body));
        });
		// 입장시 읽음 처리
        stompClient.send("/app/chat.read", {}, JSON.stringify({
            roomId: roomId,
            senderId: senderId
        }));

    }, function (error) {
        console.warn("❌ WebSocket 연결 실패", error);
        if (reconnectCount++ < 3) {
            setTimeout(connect, 3000); // 3초 후 재시도
        }
    });
}
// 메시지 출력 (메시지 수신 시 호출됨)
function showMessage(msg) {
    const chatArea = $("#chatArea");
    const isMine = (msg.senderId === senderId);
    const align = isMine ? "text-end" : "text-start";
    const bubble = isMine ? "bg-primary text-white" : "bg-light border text-dark";

    const time = msg.timestamp?.substring(11, 16) || "00:00"; // 'YYYY-MM-DDTHH:mm:ss'에서 HH:mm 추출
    const readIcon = isMine ? (msg.read ? "✔✔" : "✔") : "";

    const html = `
        <div class="${align} mb-2">
            <div class="d-inline-block p-2 rounded ${bubble}">
                <div style="font-size: 1.05rem;">${msg.content}</div>
                <small class="${isMine ? 'text-light' : 'text-muted'}">
                    ${time} ${readIcon}
                </small>
            </div>
        </div>`;

    chatArea.append(html);
    chatArea.scrollTop(chatArea[0].scrollHeight);
}

$(document).ready(function () {
	console.log("chatRoom.js 로딩됨");
	connect();
	// 채팅방 나가기 sweetAlert 처리
	$("#leaveBtn").click(function () {
	    Swal.fire({
	        title: "채팅방을 나가시겠습니까?",
	        text: "나가면 채팅방에서 사라지고, 상대도 나가면 전체 삭제됩니다.",
	        icon: "warning",
	        showCancelButton: true,
	        confirmButtonText: "네, 나갈게요",
	        cancelButtonText: "취소",
	        reverseButtons: true
	    }).then((result) => {
	        if (result.isConfirmed) {
	            $("#leaveForm").submit();
	        }
	    });
	});
	
	// 메시지 전송
	$("#chatForm").submit(function (e) {
	    e.preventDefault();
	    const content = $("#msgInput").val().trim();
	    if (!content) return;
	
		if (stompClient && stompClient.connected) {
		    stompClient.send("/app/chat.send", {}, JSON.stringify({
		        roomId: roomId,
		        senderId: senderId,
		        content: content,
		        type: "CHAT"
		    }));
		    $("#msgInput").val("");
		} else {
		    showAlert("error", "전송 실패", "💥 채팅 서버와 연결되지 않았습니다. 페이지를 새로고침 해주세요.");
		}
	});
	
});	