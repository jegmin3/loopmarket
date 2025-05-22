/**
 * 관련 뷰: /templates/pay/checkout.html
 * 결제 요청을 /api/pay/safe API로 전송하고 응답을 처리합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
  const payBtn = document.getElementById('safePayBtn');

  payBtn.addEventListener('click', async () => {
    // 잔액 부족으로 비활성화된 경우 무시
    if (payBtn.disabled) return;

    const userId = window.loginUser.id;
    const sellerId = window.checkoutProduct.sellerId;
    const productId = window.checkoutProduct.id;
    const amount = window.checkoutProduct.price;
    const tradeType = window.checkoutProduct.tradeType;
    const method = "PAY"; // 현재 자체페이 고정

    // 누락된 정보가 있는 경우 방어
    if (!userId || !sellerId || !productId || !amount) {
      await Swal.fire({
        icon: 'error',
        title: '결제 불가',
        text: '결제에 필요한 정보가 누락되었습니다.',
      });
      return;
    }

    // 배송지 입력 필요 시 → 주소 + 상세주소 모두 입력했는지 체크
    if (tradeType === '택배거래' || tradeType === 'DELIVERY') {
      const address = document.getElementById('addressInput')?.value.trim();
      const detail = document.getElementById('addressDetailInput')?.value.trim();
      if (!address || !detail) {
        await Swal.fire({
          icon: 'warning',
          title: '배송지 입력 필요',
          text: '주소를 바르게 입력해주세요.',
        });
        return;
      }
    }

    const result = await Swal.fire({
      icon: 'question',
      title: `${amount.toLocaleString()}원을 결제하시겠습니까?`,
      showCancelButton: true,
      confirmButtonText: '결제하기',
      cancelButtonText: '취소',
    });

    if (!result.isConfirmed) return;

    try {
      const res = await fetch('/api/pay/safe', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ buyerId:userId, sellerId, productId, amount })
      });

      const data = await res.json();

      if (data.success) {
        await Swal.fire({
          icon: 'success',
          title: '결제 완료',
          text: data.message,
        });
        location.href = '/mypage'; // 또는 상품 상세 페이지로
      } else {
        await Swal.fire({
          icon: 'error',
          title: '결제 실패',
          text: data.message,
        });
      }
    } catch (err) {
      console.error('결제 요청 오류:', err);
      await Swal.fire({
        icon: 'error',
        title: '오류',
        text: '서버 오류가 발생했습니다.',
      });
    }
  });
});