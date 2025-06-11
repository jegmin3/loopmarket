/**
 * 관련 뷰: /templates/pay/charge.html
 * 충전 화면의 입력 로직, 키패드 생성, 빠른 금액 버튼, 충전 버튼 클릭 처리를 담당합니다.
 */

document.addEventListener("DOMContentLoaded", () => {
	let currentAmount = 0;
	
	// 현재 잔액 조회
	fetch("/api/pay/balance")
		.then(res => res.json())
		.then(data => {
			console.log("잔액 API 응답:", data);
			if (data.success && data.balance !== undefined) {
				document.getElementById("balance").textContent = data.balance.toLocaleString();
			}
		})
		.catch(err => {
			console.error("잔액 조회 실패:", err);
			document.getElementById("balance").textContent = "0";
		});
	// 결제 수단 선택 버튼 클릭 이벤트 처리
	let selectedPg = "kakaopay"; // "kakaopay" 초기값

	document.querySelectorAll("#pgButtons button").forEach(btn => {
		btn.addEventListener("click", () => {
			selectedPg = btn.dataset.pg;

			// 모든 버튼에서 active 제거
			document.querySelectorAll("#pgButtons button").forEach(b => {
				b.classList.remove("active");
			});

			// 클릭된 버튼에 active 부여
			btn.classList.add("active");
		});
	});

	// 최초 버튼에 초기 active 부여
	document.querySelector(`#pgButtons button[data-pg="${selectedPg}"]`).classList.add("active");

	// 숫자 입력 제어
	function appendAmount(value) {
		if (value === "0" || value === "00") {
			if (currentAmount === 0) return;
			currentAmount = currentAmount * parseInt(value);
		} else {
			currentAmount = currentAmount * 10 + parseInt(value);
		}
		updateInput();
	}

	// 숫자 지우기(입력 취소)
	function deleteLastDigit() {
		currentAmount = Math.floor(currentAmount / 10);
		updateInput();
	}
	
	function updateInput() {
		const input = document.getElementById("amountInput");
		input.value = currentAmount.toLocaleString(); // 쉼표 형식 적용
	}

	// 빠른 금액 버튼
	document.querySelectorAll(".pay-quick-buttons button").forEach(btn => {
		btn.addEventListener("click", () => {
			currentAmount += parseInt(btn.dataset.amount);
			updateInput();
		});
	});

	// 키패드 동적 생성
	const keys = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "00", "0", "←"];
	const keypad = document.getElementById("keypad");
	keys.forEach(key => {
		const btn = document.createElement("button");
		btn.type = "button";
		btn.className = "btn border-0 fs-4 py-3 w-100";
		btn.innerText = key;

		btn.addEventListener("click", () => {
			if (key === "←") deleteLastDigit();
			else appendAmount(key);
		});

		// col 하나씩 만들면서 붙이기
		const col = document.createElement("div");
		col.className = "col";
		col.appendChild(btn);
		keypad.appendChild(col);
	});

	// 충전 버튼 클릭 시 → 포트원 결제창 실행
	document.getElementById("chargeBtn").addEventListener("click", () => {
		const amount = currentAmount;
		const balance = parseInt(document.getElementById("balance").textContent.replace(/,/g, ""));
		const MAX_AMOUNT = 1000000; // 최대 충전 금액 (100만원으로 설정)

		if (amount <= 0) {
			Swal.fire({
				icon: 'warning',
				title: '충전 금액 오류',
				text: '충전 금액을 입력하세요.',
			});
			return;
		}
		if (amount > MAX_AMOUNT) {
			Swal.fire({
				icon: 'warning',
				title: '충전 금액 초과',
				text: '1회 최대 충전 금액은 100만원입니다.',
			});
			return;
		}
		if (balance + amount > MAX_AMOUNT) {
			Swal.fire({
				icon: 'warning',
				title: '최대 잔액 초과',
				text: '잔액은 최대 천만원까지 보관할 수 있습니다.',
			});
			return;
		}
		// 정상 충전 요청
		requestPay(amount, selectedPg);
	});
});