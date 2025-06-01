/**
 * 관련 뷰: /templates/pay/direct-check.html
 * 즉시결제 요청을 /api/pay/direct API로 전송하고 응답을 처리합니다.
 * SweetAlert을 사용해 사용자에게 결제 상태를 안내합니다.
 */

document.addEventListener("DOMContentLoaded", async function () {
  const payBtn = document.getElementById("directPayBtn");
  const chargeBtn = document.getElementById("chargeBtn");
  const totalText = document.getElementById("totalPriceText");
  const balanceText = document.querySelector("#payBalanceText span");

  if (!payBtn || !balanceText) return;

  const productId = window.checkoutProduct.productId;
  const sellerId = window.checkoutProduct.userId;
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
    return;
  }

  // 결제 버튼 클릭 이벤트
  payBtn.addEventListener("click", async function () {
    if (!productId || !sellerId) {
      await Swal.fire({
        icon: "error",
        title: "결제 불가",
        text: "결제에 필요한 정보가 누락되었습니다.",
      });
      return;
    }

    const result = await Swal.fire({
      icon: "question",
      title: `${amount.toLocaleString()}원을 결제하시겠습니까?`,
      showCancelButton: true,
      confirmButtonText: "결제하기",
      cancelButtonText: "취소",
    });

    if (!result.isConfirmed) return;

    try {
      const res = await fetch("/api/pay/direct", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ sellerId, productId }),
      });

      const data = await res.json();

      if (data.success) {
        await Swal.fire({
          icon: "success",
          title: "결제 완료",
          text: data.message,
        });
        location.href = "/";
      } else {
        await Swal.fire({
          icon: "error",
          title: "결제 실패",
          text: data.message || "결제 중 오류가 발생했습니다.",
        });
      }
    } catch (err) {
      console.error("즉시결제 실패", err);
      await Swal.fire({
        icon: "error",
        title: "서버 오류",
        text: "결제 요청 중 문제가 발생했습니다.",
      });
    }
  });
});