/**
 * 관련 뷰: /templates/pay/checkout.html
 * 배송지 표시, 잔액 조회, 버튼 상태 변경 등 UI 제어를 담당합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
  const addressContainer = document.getElementById('addressContainer');
  const payBtn = document.getElementById('safePayBtn');
  const chargeBtn = document.getElementById('chargeBtn');
  const payBalanceText = document.getElementById('payBalanceText');

  // 택배 거래일 경우 배송지 입력 표시
  if (window.checkoutProduct.isDelivery) {
    addressContainer.style.display = 'block';
  }

  // 잔액 조회 후 UI 처리
  fetch(`/api/pay/balance`)
    .then(res => res.json())
    .then(data => {
      if (data.success && data.balance !== undefined) {
        const balance = data.balance;
        const productPrice = window.checkoutProduct.price;
        const shippingFee = window.checkoutProduct.shippingFee || 0;

        const totalPrice = window.checkoutProduct.isDelivery
          ? productPrice + shippingFee
          : productPrice;

        // 잔액 표시
		payBalanceText.textContent = `${balance.toLocaleString('ko-KR')}원`;
		
		// 총 결제 금액 표시
		const totalPriceText = document.getElementById('totalPriceText');
		if (totalPriceText) {
			totalPriceText.textContent = `${totalPrice.toLocaleString('ko-KR')}원`;
		}
		
        // 잔액 부족 시 처리
        if (balance < totalPrice) {
          payBtn.disabled = true;
          chargeBtn.style.display = 'block';

          chargeBtn.addEventListener('click', () => {
            location.href = '/pay/charge';
          });
        }
      } else {
        console.warn('잔액 조회 실패:', data.message);
      }
    })
    .catch(err => {
      console.error('잔액 조회 오류:', err);
    });
	
	// "안전결제란 무엇인가요?" 링크 클릭 시 설명 카드 토글
	  const infoLink = document.getElementById('safePayInfoLink');
	  const infoCard = document.getElementById('safePayInfoCard');
	  if (infoLink && infoCard) {
	    infoLink.addEventListener('click', () => {
	      const isHidden = infoCard.style.display === 'none';
	      infoCard.style.display = isHidden ? 'block' : 'none';
	    });
	  }
});