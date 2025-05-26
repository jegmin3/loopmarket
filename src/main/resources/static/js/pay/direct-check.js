document.addEventListener("DOMContentLoaded", function () {
  const payBtn = document.getElementById("directPayBtn");
  const chargeBtn = document.getElementById("chargeBtn");
  const totalText = document.getElementById("totalPriceText");
  const balanceText = document.querySelector("#payBalanceText span");

  if (!payBtn || !balanceText) return;

  const productId = window.checkoutProduct.productId;
  const sellerId = window.checkoutProduct.userId;
  const buyerId = window.loginUser.id;
  const amount = window.checkoutProduct.price;

  totalText.innerText = `₩${amount.toLocaleString()}`;

  // 현재 잔액
  const balance = parseInt(balanceText.innerText.replace(/[^\d]/g, ""));

  // 잔액 부족 시 → 결제 버튼 비활성화, 충전 버튼 표시
  if (balance < amount) {
    payBtn.disabled = true;
    chargeBtn.style.display = "block";

    chargeBtn.addEventListener("click", function () {
      location.href = "/pay/charge";
    });
  }

  // 💳 결제 버튼 클릭 이벤트
  payBtn.addEventListener("click", function () {
    if (!buyerId || !productId || !sellerId) {
      alert("결제에 필요한 정보가 부족합니다.");
      return;
    }

    fetch("/api/pay/direct", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ buyerId, sellerId, productId }),
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.success) {
          alert("즉시결제가 완료되었습니다.");
          payBtn.disabled = true;
          payBtn.innerText = "결제 완료";
		  location.href = "/";
        } else {
          alert(data.message || "결제 중 오류가 발생했습니다.");
        }
      })
      .catch((err) => {
        console.error("즉시결제 실패", err);
        alert("결제 요청 중 오류가 발생했습니다.");
      });
  });
});