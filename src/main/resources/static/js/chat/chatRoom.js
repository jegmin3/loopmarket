let stompClient = null;
// chatRoom.html의 하단 스크립트에 값 설정해둠
let roomId = window.roomId; // 동기화 가능하게 let으로.
const senderId = window.senderId;
//const targetId = window.targetId;
// WebSocket 연결
let reconnectCount = 0;
let socketConnected = false;
//const shownMessageIds = new Set();
const shownMessageIds = new Set(window.shownMessageIds || []);

// 웹소켓 연결 함수
function connect(callback) {
		// 이미 연결되어 있다면
		if (stompClient && stompClient.connected) {
		    console.log("🔁 이미 연결됨");
		    if (callback) callback();
		    return;
		}
		// 사용자가 서버에 웹소켓 연결
	    const socket = new SockJS("/ws/chat");
	    stompClient = Stomp.over(socket);
		
		// 소켓 연결 시도 중 전송버튼 비활성화
		$("#sendBtn").prop("disabled", true);
		socketConnected = false;
		
		// 서버 연결 후 구독 시작
	    stompClient.connect({}, function () {
	        console.log("✅ WebSocket 연결됨");
	
			socketConnected = true; // 소켓 연결 완료 표시
			$("#sendBtn").prop("disabled", false); // 전송 버튼 활성화
			
			// 해당 채팅방 구독
	        stompClient.subscribe(`/queue/room.${roomId}`, function (message) {
				const msg = JSON.parse(message.body);
				if (msg.type === "READ") return; // 읽음 메시지 무시
				showMessage(msg);
	        });
			// 입장시 읽음 처리(roomId, senderId담아서 서버로 전송)
	        stompClient.send("/app/chat.read", {}, JSON.stringify({
	            roomId: roomId,
	            senderId: senderId
	        }));
			
			if (typeof callback === "function") {
			    callback();
			}
	
	    }, function (error) {
	        console.warn("❌ WebSocket 연결 실패", error);
			socketConnected = false;
			$("#sendBtn").prop("disabled", true); // 실패 시 전송버튼 다시 잠금
	        if (reconnectCount++ < 3) {
	            setTimeout(connect, 3000); // 3초 후 재시도
	        }
	    });
}

// 방이 없으면 생성하는 Ajax 함수(상품 상세보기에서 채팅하기 누를 시 사용함)
// content: 사용자가 보내려는 채팅 내용
// callback: 메시지를 실제로 전송하는 동작을 나중에 실행하기 위한 함수(콜백)
function ensureChatRoom(content, callback) {
	// 이미 roomId가 있는 경우, 소켓연결로 간주,바로 메시지 전송 콜백 실행
    if (window.roomId) {
        callback(content);
    } else if (!window.roomId) {
		// 채팅방 ID가 없으면 /chat/api/create-room API에 targetId를 넘겨 방을 생성 요청
        $.post("/chat/api/create-room", { targetId: window.targetId }, function (res) {
			//서버가 roomId를 응답했을때
            if (res.roomId) {
				//window.roomId, roomId 전역 변수에 저장
                window.roomId = res.roomId;
				roomId = res.roomId;  // stompClient에서 올바른 roomId 구독하게 하기 위해 필요
                console.log("채팅방 생성됨: roomId =", res.roomId);
				
				//connect() 함수를 호출해서 WebSocket 연결을 시작
				connect(() => {
					// 연결이 완료되면 callback(content)를 호출해 메시지를 전송
				    callback(content);
				});
            } else {
                showAlert("error", "채팅방 생성 실패", "서버로부터 roomId를 받을 수 없습니다.");
            } 
		});
    } else if (!socketConnected) {
		// 방은 있는데 소켓 연결이 끊긴 상태라면 -> 다시 연결 후 메시지 전송
		connect(() => {
			callback(content);
		});
	}
}

// 메시지 출력 (메시지 수신 시 호출됨)
function showMessage(msg) {
    const chatArea = $("#chatArea");
    const isMine = (msg.senderId === senderId);
	if (shownMessageIds.has(msg.msgId)) {
	    if (isMine && msg.read) {
	        // 이미 보낸 메시지인데 read=true로 갱신되면 읽음 표시만 업데이트
	        $(`#msg-${msg.msgId} .read-status`).text("읽음");
	    }
	    return; // 새로 append하지 않음
	}
	
    const align = isMine ? "text-end" : "text-start";
    const bubble = isMine ? "bg-primary text-white" : "bg-light border text-dark";

    const time = msg.timestamp?.substring(11, 16) || "00:00"; // 'YYYY-MM-DDTHH:mm:ss'에서 HH:mm 추출
    const readIcon = isMine ? (msg.read ? "읽음" : "안읽음") : "";
	//const alreadyShown = shownMessageIds.has(msg.msgId);
		
	// 최초 출력
	const html = `
	    <div class="${align} mb-2">
	        <div class="d-inline-block rounded ${bubble}" style="max-width: 80%; padding: 0.5rem 0.75rem;">
	            <div style="font-size: 1.05rem;">${msg.content}</div>
	        </div>
	        <div style="font-size: 0.75rem;" class="${isMine ? 'text-end text-muted' : 'text-start text-muted'} mt-1">
	            ${time} ${readIcon}
	        </div>
	    </div>`;

    chatArea.append(html);
    chatArea.scrollTop(chatArea[0].scrollHeight);
}

$(document).ready(function () {
	console.log("chatRoom.js 로딩됨");
	// roomId가 있을 때만 바로 WebSocket 연결(제어문 안쓰면 채팅하기 누르자마자 방 만들어짐.)
	if (window.roomId) {
	    connect(); // 새로고침 해도 소켓 연결 자동 실행
	}
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

		// 메시지 전송 시, 방이 없으면 생성되게.
		ensureChatRoom(content, function (content) {
			// 연결 완료 보장 이후 전송
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
	
});	