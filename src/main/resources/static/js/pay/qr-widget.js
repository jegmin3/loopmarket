// 채팅방 QR 생성 위젯(qr-widget.html)과 연결됩니다.

function initQRWidget() {
	const generateBtn = document.getElementById("generateQRBtn");
	const productSelect = document.getElementById("productSelect");
	const qrCodeContainer = document.getElementById("qrCode");

	if (!generateBtn || !productSelect || !qrCodeContainer) return;

	generateBtn.addEventListener("click", function () {
		const productId = productSelect.value;
		const sellerId = window.loginUserId;

		if (!productId || !sellerId) {
			alert("상품이나 판매자 정보가 누락되었습니다.");
			return;
		}

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
}

if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", initQRWidget);
} else {
	initQRWidget();
}