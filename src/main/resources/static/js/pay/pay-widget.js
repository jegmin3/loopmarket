// 마이페이지의 페이 위젯(pay-widget.html)과 연결됩니다.

function initPayWidget() {
	const chargeBtn = document.getElementById("pay-charge-btn");
	const transferBtn = document.getElementById("pay-transfer-btn");
	const balanceText = document.getElementById("pay-balance");

	// 충전 버튼 클릭 시 이동
	if (chargeBtn) {
		chargeBtn.addEventListener("click", function() {
			location.href = "/pay/charge";
		});
	}

	// 환불 버튼 클릭 시 이동
	if (transferBtn) {
		transferBtn.addEventListener("click", function() {
			location.href = "/pay/refund";
		});
	}

	// 세션 기반 잔액 조회
	if (balanceText) {
		fetch("/api/pay/balance")
			.then((res) => res.json())
			.then((data) => {
				if (data.success && data.balance !== undefined) {
					balanceText.innerText = `${data.balance.toLocaleString()}원`;
				} else {
					console.warn("잔액 조회 실패:", data.message);
				}
			})
			.catch((err) => {
				console.warn("잔액 조회 오류:", err);
			});
	}
}

// DOMContentLoaded 여부 확인 → 이미 로드된 경우 바로 실행
if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initPayWidget);
} else {
	initPayWidget();
}