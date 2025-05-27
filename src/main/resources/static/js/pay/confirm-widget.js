// confirm-widget.html과 연결된 스크립트입니다.

document.addEventListener("DOMContentLoaded", () => {
  const confirmBtn = document.getElementById("confirmPayBtn");
  const hiddenInput = document.getElementById("selectedConfirmPaymentId");

  if (!confirmBtn || !hiddenInput) return;

  // 카드 클릭 시 → 선택된 카드 강조 + hidden input 저장
  window.selectConfirmItem = function (card) {
    document.querySelectorAll("#confirmCardList .card").forEach(el => {
      el.classList.remove("border-success", "shadow-sm");
    });
    card.classList.add("border-success", "shadow-sm");

    const paymentId = card.getAttribute("data-id");
    hiddenInput.value = paymentId;
  };

  // 구매 확정 버튼 클릭
  confirmBtn.addEventListener("click", async () => {
    const paymentId = hiddenInput.value;
    const buyerId = window.loginUserId;

    if (!paymentId || !buyerId) {
      await Swal.fire({
        icon: "warning",
        title: "선택 필요",
        text: "구매 확정할 상품을 선택해주세요.",
      });
      return;
    }

    const result = await Swal.fire({
      icon: "question",
      title: "이 상품을 구매 확정하시겠습니까?",
      showCancelButton: true,
      confirmButtonText: "확정",
      cancelButtonText: "취소"
    });

    if (!result.isConfirmed) return;

    try {
      const res = await fetch("/api/pay/complete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ paymentId: parseInt(paymentId), buyerId: parseInt(buyerId) })
      });

      const data = await res.json();

      if (data.success) {
        await Swal.fire({
          icon: "success",
          title: "구매 확정 완료",
          text: data.message || "판매자에게 금액이 정산되었습니다."
        });
        location.reload();
      } else {
        await Swal.fire({
          icon: "error",
          title: "실패",
          text: data.message || "구매 확정 중 오류가 발생했습니다."
        });
      }
    } catch (err) {
      console.error("구매 확정 요청 실패", err);
      await Swal.fire({
        icon: "error",
        title: "오류 발생",
        text: "서버와의 통신 중 문제가 발생했습니다."
      });
    }
  });
});