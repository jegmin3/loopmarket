/**
 * 관련 뷰: /templates/pay/checkout.html
 * 결제 화면의 입력 로직, 배송지 입력, 결제 수단 선택 등을 처리합니다.
 */

document.addEventListener('DOMContentLoaded', () => {
  const tradeType = window.checkoutProduct.tradeType;
  const addressContainer = document.getElementById('addressContainer');

  // 택배 거래일 경우 배송지 입력란 표시
  if (tradeType === '택배') {
    addressContainer.style.display = 'block';
  }

  // 기본 결제 수단 선택 UI 제어는 필요 시 확장 가능
});