/**
 * 관련 뷰: /templates/pay/charge.html
 * 포트원 결제창을 띄우고 결제 정보를 백엔드로 전송하는 스크립트입니다.
 */

// charge.html 에서 포트원 스크립트를 브라우저에 로딩 -> 자동으로 window.IMP라는 전역 객체 등록됨.
const IMP = window.IMP;

// 포트원 테스트 상점 코드
IMP.init("imp62256541");

function requestPay(userId, amount, selectedPg) {
	IMP.request_pay({
		pg: selectedPg,
		pay_method: "card",
		merchant_uid: "order_" + new Date().getTime(),
		name: "LoopMarket 충전",
		amount: amount,
		buyer_email: window.loginUserEmail,
		buyer_name: window.loginUserName
	}, function(rsp) {
		if (rsp.success) {
			// 결제 성공 → 서버로 충전 요청
			fetch("/api/pay/charge", {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify({
					userId: userId,
					amount: amount,
					method: selectedPg.toUpperCase()
				})
			})
				.then(res => res.json())
				.then(data => {
					alert(data.message);
					// 현재 잔액 갱신
					if (data.currentBalance !== undefined) {
						const balanceSpan = document.getElementById("currentBalance");
						if (balanceSpan) {
							balanceSpan.textContent = data.currentBalance.toLocaleString();
						}
					}
				}); // fetch.then
		} else {
			alert("결제가 실패되었습니다: " + rsp.error_msg);
		}
	}); // IMP.request_pay
} // requestPay 함수