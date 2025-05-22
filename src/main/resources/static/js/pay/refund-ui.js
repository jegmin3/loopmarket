/**
 * 관련 뷰: /templates/pay/refund.html
 * 
 * 환불 화면의 입력 로직, 키패드 생성, 빠른 금액 버튼, 잔액 불러오기,
 * 환불 버튼 클릭 후 /api/pay/refund API 호출까지 담당합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
	const amountInput = document.getElementById('amountInput');
	const refundBtn = document.getElementById('refundBtn');
	const keypad = document.getElementById('keypad');
	const quickButtons = document.querySelectorAll('.pay-quick-buttons button');
	const userId = window.loginUserId;

	let currentAmount = 0;

	// 현재 잔액 불러오기
	async function loadBalance() {
		try {
			const res = await fetch(`/api/pay/balance/${userId}`);
			const data = await res.json();
			if (data.success) {
				document.getElementById('balance').textContent = data.balance.toLocaleString();
			} else {
				console.warn('잔액 조회 실패:', data.message);
			}
		} catch (err) {
			console.error('잔액 조회 오류:', err);
		}
	}

	// 숫자 키패드 렌더링
	const renderKeypad = () => {
		const keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0', '←'];
		keypad.innerHTML = '';

		keys.forEach(key => {
			const btn = document.createElement('button');
			btn.type = 'button';
			btn.className = "btn border-0 fs-4 py-3 w-100";
			btn.textContent = key;
			btn.onclick = () => handleKeyInput(key);

			const col = document.createElement('div');
			col.className = 'col';
			col.appendChild(btn);
			keypad.appendChild(col);
		});
	};

	// 키패드 입력 처리
	const handleKeyInput = (key) => {
		if (key === '←') {
			currentAmount = Math.floor(currentAmount / 10);
		} else if (key === '00') {
			currentAmount = currentAmount * 100;
		} else {
			const digit = parseInt(key);
			if (!isNaN(digit)) {
				currentAmount = currentAmount * 10 + digit;
			}
		}
		amountInput.value = currentAmount.toLocaleString();
	};

	// 빠른 금액 버튼 클릭 시 누적 입력
	quickButtons.forEach(btn => {
		btn.addEventListener('click', () => {
			const amount = parseInt(btn.dataset.amount);
			currentAmount += amount;
			amountInput.value = currentAmount.toLocaleString();
		});
	});

	// 환불 버튼 클릭 → SweetAlert2로 처리
	refundBtn.addEventListener('click', async () => {
		const balanceText = document.getElementById('balance').textContent;
		const currentBalance = parseInt(balanceText.replace(/,/g, '')) || 0;

		if (isNaN(currentAmount) || currentAmount <= 0) {
			await Swal.fire({
				icon: 'warning',
				title: '금액 오류',
				text: '환불할 금액을 입력하세요.',
			});
			return;
		}

		if (currentAmount > currentBalance) {
			await Swal.fire({
				icon: 'warning',
				title: '잔액 초과',
				text: '현재 잔액보다 큰 금액은 환불할 수 없습니다.',
			});
			return;
		}

		const result = await Swal.fire({
			icon: 'question',
			title: `${currentAmount.toLocaleString()}원을 환불하시겠습니까?`,
			showCancelButton: true,
			confirmButtonText: '환불하기',
			cancelButtonText: '취소',
		});

		if (!result.isConfirmed) return;

		try {
			const res = await fetch('/api/pay/refund', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({
					userId: userId,
					amount: currentAmount
				})
			});

			const data = await res.json();

			if (data.success) {
				await Swal.fire({
					icon: 'success',
					title: '환불 완료',
					text: `${data.message}\n현재 잔액: ${data.balance.toLocaleString()}원`,
				});
				currentAmount = 0;
				amountInput.value = '0';
				document.getElementById('balance').textContent = data.balance.toLocaleString();
			} else {
				await Swal.fire({
					icon: 'error',
					title: '환불 실패',
					text: data.message,
				});
			}
		} catch (err) {
			console.error('환불 요청 오류:', err);
			await Swal.fire({
				icon: 'error',
				title: '서버 오류',
				text: '환불 요청 중 문제가 발생했습니다.',
			});
		}
	});

	// 초기 실행
	renderKeypad();
	loadBalance();
});