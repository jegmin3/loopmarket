/**
 * 관련 뷰: /templates/pay/charge.html
 * 포트원 결제창을 띄우고 결제 정보를 백엔드로 전송하는 스크립트입니다.
 */

const IMP = window.IMP;
IMP.init("imp62256541"); // 포트원 테스트 상점 코드

function requestPay(amount, selectedPg) {
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
					amount: amount,
					method: selectedPg.toUpperCase()
				})
			})
				.then(res => res.json())
				.then(data => {
					// 결제 성공 sweetalert 메시지
					Swal.fire({
						icon: 'success',
						title: '충전 완료!',
						text: data.message,
						timer: 1500,
						showConfirmButton: false
					});

					// 잔액은 서버에 다시 조회해서 반영
					fetch("/api/pay/balance")
						.then(res => res.json())
						.then(balanceData => {
							if (balanceData.success && balanceData.balance !== undefined) {
								document.getElementById("balance").textContent =
									balanceData.balance.toLocaleString();
							}
						});
				});
		} else {
			// 결제 실패 sweetalert 메시지
			Swal.fire({
				icon: 'error',
				title: '결제 실패',
				text: rsp.error_msg,
				confirmButtonText: '확인'
			});
		}
	});
}