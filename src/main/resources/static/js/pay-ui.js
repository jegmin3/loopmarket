/**
 * 관련 뷰: /templates/pay/charge.html
 * 충전 화면의 입력 로직, 키패드 생성, 빠른 금액 버튼, 충전 버튼 클릭 처리를 담당합니다.
 */

document.addEventListener("DOMContentLoaded", () => {
	// 숫자 입력 제어
	function appendAmount(value) {
		const input = document.getElementById("amountInput");
		if (input.value === "0") input.value = value;
		else input.value += value;
	}

	// 숫자 지우기(입력 취소)
	function deleteLastDigit() {
		const input = document.getElementById("amountInput");
		input.value = input.value.length > 1 ? input.value.slice(0, -1) : "0";
	}

	// 빠른 금액 버튼
	document.querySelectorAll(".pay-quick-buttons button").forEach(btn => {
		btn.addEventListener("click", () => {
			const input = document.getElementById("amountInput");
			input.value = parseInt(input.value || "0") + parseInt(btn.dataset.amount);
		});
	});

	// 키패드 동적 생성
	const keys = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "←"];
	const keypad = document.getElementById("keypad");
	keys.forEach(key => {
		const btn = document.createElement("button");
		btn.innerText = key;
		btn.addEventListener("click", () => {
			if (key === "←") deleteLastDigit();
			else appendAmount(key);
		});
		keypad.appendChild(btn);
	});

	// 충전 버튼 클릭 시 → 포트원 결제창 실행
	document.getElementById("chargeBtn").addEventListener("click", () => {
		const amount = parseInt(document.getElementById("amountInput").value || "0");
		const selectedPg = document.getElementById("pgSelect").value;

		if (amount > 0) {
			requestPay(window.loginUserId, amount, selectedPg); // pay.js 함수 호출
		} else {
			alert("충전 금액을 입력하세요.");
		}
	});
});