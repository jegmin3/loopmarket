// 구매 확정 버튼 위젯(confirm-widget.html)과 연결됩니다.

function initConfirmPayWidget() {
	const confirmBtn = document.getElementById("confirm-pay-btn");

	if (!confirmBtn) return;

	confirmBtn.addEventListener("click", () => {
		const paymentId = confirmBtn.dataset.paymentId;
		const buyerId = confirmBtn.dataset.buyerId;

		if (!paymentId || !buyerId) {
			alert("구매 확정에 필요한 정보가 없습니다.");
			return;
		}

		fetch("/api/pay/complete", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				paymentId: parseInt(paymentId),
				buyerId: parseInt(buyerId)
			})
		})
			.then((res) => res.json())
			.then((data) => {
				if (data.success) {
					alert("구매 확정 완료! 판매자에게 정산이 완료되었습니다.");
					confirmBtn.disabled = true;
					confirmBtn.innerText = "구매 확정 완료";
				} else {
					alert(data.message || "구매 확정 처리 중 오류가 발생했습니다.");
				}
			})
			.catch((err) => {
				console.error("구매 확정 실패", err);
				alert("요청 중 오류가 발생했습니다.");
			});
	});
}

if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initConfirmPayWidget);
} else {
	initConfirmPayWidget();
}