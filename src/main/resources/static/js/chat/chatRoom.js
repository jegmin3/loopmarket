// chatRoom.html의 하단 스크립트에 전역변수 설정해둠
let stompClient = null;
let roomId = window.roomId; // 동기화 가능하게 let으로.
const senderId = window.senderId;
//const targetId = window.targetId;
// WebSocket 연결
let reconnectCount = 0;
let socketConnected = false;
//const shownMessageIds = new Set();
const shownMessageIds = new Set(window.shownMessageIds || []);
let hasSentRead = false; // 중복 방지 플래그

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
			
			// 입장 후 바로 읽음 처리 요청 전송
			stompClient.send("/app/chat.read", {}, JSON.stringify({
			    roomId: roomId,
			    senderId: senderId
			}));
			
			// 해당 채팅방 구독
	        stompClient.subscribe(`/queue/room.${roomId}`, function (message) {
				const msg = JSON.parse(message.body);
				console.log("💬 수신된 메시지:", msg); //로깅
				
				// 내 메시지 중 마지막 메시지에 대해 읽음 표시만 갱신
				if (msg.type === "READ") {
					console.log("READ 확인 후 갱신됨");
				    const lastMyMsg = $("#chatArea .text-end[id^='msg-']").filter(function () {
				        return $(this).data("sender-id") == senderId;
				    }).last();

				    if (lastMyMsg.length > 0) {
				        lastMyMsg.find(".read-status").text("읽음");
				    }
				    return;
				}
				
				// 상대가 채팅방을 나갔을 시 출력될 메시지
				if (msg.type === "LEAVE") {
				    const html = `
				        <div class="text-center text-muted my-2" style="font-size: 0.85rem;">
				            <i class="bi bi-person-x me-1"></i> ${msg.content}
				        </div>`;
				    $("#chatArea").append(html);
				    return;
				}
				
				// isMine: 현재 메시지가 나한텧서 보내진건지 확인
				const isMine = (msg.senderId === senderId);
				// 수신한 상대방 메시지면 → 실시간 읽음 처리 트리거
				if (!isMine && stompClient?.connected) {
					console.log("읽음 처리 전송");
				    stompClient.send("/app/chat.read", {}, JSON.stringify({
				        roomId: roomId,
				        senderId: senderId
				    }));
					hasSentRead = true; // 한 번만 보냄
				}
				
				// 기본 채팅 메시지 출력
				showMessage(msg);
	        });
			
			if (typeof callback === "function") {
			    callback();
			}
	
	    }, function (error) {
	        console.warn("❌ WebSocket 연결 실패 또는 종료됨", error);
			socketConnected = false;
			$("#sendBtn").prop("disabled", true); // 실패 시 전송버튼 다시 잠금
			// 웹소켓 연결 재시도(최대 3회로 설정해둠)
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
        $.post("/chat/api/create-room", { targetId: window.targetId, productId: productId }, function (res) {
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
	// chatArea: 채팅 말풍선이 들어가는 곳
    const chatArea = $("#chatArea");
	const isMine = (msg.senderId === senderId); // subscribe와 별개로 여기서도 정의
	// 중복 메시지면 새로 append하지 않음
	if (shownMessageIds.has(msg.msgId)) {
	    // 내가 보낸 메시지고, 읽음 처리된 경우라면 UI 업데이트
	    if (isMine && msg.read) {
	        const $msgEl = $(`#msg-${msg.msgId}`);
	        const isLastMine = $msgEl.closest(".text-end").is($("#chatArea .text-end").last());

	        if (isLastMine) {
	            $msgEl.find(".read-status").text("읽음");
	        }
	    }
	    return; // append 안하고 종료
	}
	
	// 말품선 스타일
    const align = isMine ? "text-end" : "text-start";
    const bubble = isMine ? "bg-primary text-white" : "bg-light border text-dark";
	// 메시지 전송시간 처리
	// msg.timestamp의형식(ISO)인'YYYY-MM-DDTHH:mm:ss'에서 HH:mm만 추출
    const time = msg.timestamp?.substring(11, 16) || "00:00"; 
		
	// HTML 말풍선
	const html = `
	    <div class="${align} mb-2" id="msg-${msg.msgId}" data-sender-id="${msg.senderId}">
	        <div class="d-inline-block rounded ${bubble}" style="max-width: 80%; padding: 0.5rem 0.75rem;">
	            <div style="font-size: 1.05rem;">${msg.content}</div>
	        </div>
	        <div style="font-size: 0.75rem;" class="${align} text-muted mt-1">
	            ${time} <span class="read-status"></span>
	        </div>
	    </div>`;

    chatArea.append(html); //말풍선을 chatArea에 추가
	shownMessageIds.add(msg.msgId); // 메시지 중복 렌더링 방지
	//append 이후에, 가장 마지막 내 메시지만 읽음 상태 업데이트
	if (msg.senderId === senderId) {
	    // 먼저 모든 표시 제거
	    $(".read-status").text("");

	    // 마지막 내 메시지 찾아서 표시
	    const lastMyMsg = chatArea.find(".text-end[id^='msg-']").filter(function () {
	        return $(this).data("sender-id") == senderId;
	    }).last();

	    if (lastMyMsg.length > 0) {
	        const readStatus = msg.read ? "읽음" : "안읽음";
	        lastMyMsg.find(".read-status").text(readStatus);
		}
	}	
    chatArea.scrollTop(chatArea[0].scrollHeight); // 스크롤을 가장 아래로 내려줌(최신 메시지 보기 편하게)
}


/* 스크립트 처음 로드시 작업할 동작 */
$(document).ready(function () {
	console.log("chatRoom.js 로딩됨");
	// roomId가 있을 때만 바로 WebSocket 연결(제어문 안쓰면 채팅하기 누르자마자 방 만들어짐.)
	if (window.roomId) {
	    connect(); // 새로고침 해도 소켓 연결 자동 실행
	}
	// 채팅방 입장 시 스크롤을 맨 아래로
	const chatArea = $("#chatArea");
	chatArea.scrollTop(chatArea[0].scrollHeight);
	
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