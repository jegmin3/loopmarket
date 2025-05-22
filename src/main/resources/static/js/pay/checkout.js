/**
 * 관련 뷰: /templates/pay/checkout.html
 * 결제 요청을 /api/pay/safe API로 전송하고 응답을 처리합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
  const payBtn = document.getElementById('safePayBtn');

  payBtn.addEventListener('click', async () => {
    const userId = window.loginUser.id;
    const productId = window.checkoutProduct.id;
    const amount = window.checkoutProduct.price;
    const tradeType = window.checkoutProduct.tradeType;
    const method = document.querySelector('input[name="payMethod"]:checked').value;

    if (tradeType === '택배') {
      const address = document.getElementById('addressInput').value.trim();
      if (!address) {
        await Swal.fire({ icon: 'warning', text: '배송지를 입력해주세요.' });
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
        body: JSON.stringify({
          userId,
          productId,
          amount,
          method
        })
      });

      const data = await res.json();

      if (data.success) {
        await Swal.fire({ icon: 'success', title: '결제 완료', text: data.message });
        location.href = '/mypage'; // or 구매내역 페이지
      } else {
        await Swal.fire({ icon: 'error', title: '결제 실패', text: data.message });
      }
    } catch (err) {
      console.error('결제 요청 오류:', err);
      await Swal.fire({ icon: 'error', title: '오류', text: '서버 오류가 발생했습니다.' });
    }
  });
});