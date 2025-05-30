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

		if (!productId) {
			Swal.fire({
			  icon: 'warning',
			  title: 'QR 생성 실패',
			  text: '상품을 선택해주세요.',
			  confirmButtonText: '확인'
			});
			return;
		}

		// UI 조정
		document.querySelectorAll("#productCardList .card").forEach(card => {
			card.style.display = (card.getAttribute("data-id") === productId) ? "block" : "none";
		});
		productLabel.style.display = "none";
		generateBtn.style.display = "none";
		resetBtn.style.display = "block";

		// sellerId 제거 → 서버가 세션으로 판단
		const expiresAt = new Date(Date.now() + 5 * 60 * 1000).toISOString();
		const payload = { productId: parseInt(productId), expiresAt };
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

		productLabel.style.display = "block";
		generateBtn.style.display = "block";
		resetBtn.style.display = "none";
	});
}

if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initQRWidget);
} else {
	initQRWidget();
}