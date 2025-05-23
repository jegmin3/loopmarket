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

	// 송금 버튼 클릭 시 이동
	if (transferBtn) {
		transferBtn.addEventListener("click", function() {
			location.href = "/pay/refund";
		});
	}

	// 로그인한 사용자 ID가 존재하면 잔액 조회
	if (window.loginUserId && balanceText) {
		fetch(`/api/pay/balance/${window.loginUserId}`)
			.then((res) => res.json())
			.then((data) => {
				balanceText.innerText = data.balance + "원";
			})
			.catch((err) => {
				console.warn("잔액 조회 실패", err);
			});
	}
}

// DOMContentLoaded 여부 확인 → 이미 로드된 경우 바로 실행
if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initPayWidget);
} else {
	initPayWidget();
}