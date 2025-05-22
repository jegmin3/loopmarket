/**
 * 관련 뷰: /templates/pay/checkout.html
 * 배송지 표시, 잔액 조회, 버튼 상태 변경 등 UI 제어를 담당합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
  const tradeType = window.checkoutProduct.tradeType;
  const addressContainer = document.getElementById('addressContainer');
  const payBtn = document.getElementById('safePayBtn');
  const chargeBtn = document.getElementById('chargeBtn');
  const payBalanceText = document.getElementById('payBalanceText');

  // 택배 거래일 경우 배송지 입력 표시
  if (tradeType === '택배거래' || tradeType === 'DELIVERY') {
    addressContainer.style.display = 'block';
  }

  // 잔액 조회 후 UI 처리
  fetch(`/api/pay/balance/${window.loginUser.id}`)
    .then(res => res.json())
    .then(data => {
      if (data.success && data.balance !== undefined) {
        const balance = data.balance;
        const price = window.checkoutProduct.price;

        // 잔액 표시
        payBalanceText.textContent = `₩${balance.toLocaleString()}`;

        // 잔액 부족 시 처리
        if (balance < price) {
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
});