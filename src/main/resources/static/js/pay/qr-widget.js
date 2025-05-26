// QR 생성 위젯(qr-widget.html)과 연결됩니다.
function initQRWidget() {
	const generateBtn = document.getElementById("generateQRBtn");
	const resetBtn = document.getElementById("resetQRSelectionBtn");
	const qrCodeContainer = document.getElementById("qrCode");
	const hiddenInput = document.getElementById("selectedProductId");
	const productLabel = document.getElementById("productLabel"); // 상품 선택 라벨

	if (!generateBtn || !qrCodeContainer || !hiddenInput || !resetBtn || !productLabel) return;

	// 카드 클릭 이벤트 등록 (상품 선택)
	window.selectProduct = function (card) {
		document.querySelectorAll("#productCardList .card").forEach(el => {
			el.classList.remove("border-primary", "shadow-sm");
		});
		card.classList.add("border-primary", "shadow-sm");

		const productId = card.getAttribute("data-id");
		hiddenInput.value = productId;
	};

	// QR 생성 버튼 클릭 시
	generateBtn.addEventListener("click", function () {
		const productId = hiddenInput.value;
		const sellerId = window.loginUserId;

		if (!productId || !sellerId) {
			Swal.fire({
			  icon: 'warning',
			  title: 'QR 생성 실패',
			  text: '상품이나 판매자 정보가 누락되었습니다.',
			  confirmButtonText: '확인'
			});
			return;
		}

		// UI 조정
		document.querySelectorAll("#productCardList .card").forEach(card => {
			card.style.display = (card.getAttribute("data-id") === productId) ? "block" : "none";
		});
		productLabel.style.display = "none";        // 상품 선택 라벨 숨김
		generateBtn.style.display = "none";         // QR 생성 버튼 숨김
		resetBtn.style.display = "block";           // 되돌리기 버튼 표시

		const expiresAt = new Date(Date.now() + 5 * 60 * 1000).toISOString();
		const payload = { productId: parseInt(productId), sellerId: parseInt(sellerId), expiresAt };
		const token = btoa(JSON.stringify(payload));
		const qrUrl = `${location.origin}/pay/direct-check?token=${encodeURIComponent(token)}`;

		qrCodeContainer.innerHTML = "";
		new QRCode(qrCodeContainer, {
			text: qrUrl,
			width: 256,
			height: 256
		});
	});

	// 되돌리기
	resetBtn.addEventListener("click", function () {
		document.querySelectorAll("#productCardList .card").forEach(card => {
			card.style.display = "block";
			card.classList.remove("border-primary", "shadow-sm");
		});
		hiddenInput.value = "";
		qrCodeContainer.innerHTML = "";

		productLabel.style.display = "block";       // 다시 보이게
		generateBtn.style.display = "block";        // 다시 보이게
		resetBtn.style.display = "none";            // 숨김
	});
}

if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initQRWidget);
} else {
	initQRWidget();
}