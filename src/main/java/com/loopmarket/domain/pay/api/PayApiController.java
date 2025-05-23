package com.loopmarket.domain.pay.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.pay.dto.BalanceResponse;
import com.loopmarket.domain.pay.dto.ChargeRequest;
import com.loopmarket.domain.pay.dto.ChargeResponse;
import com.loopmarket.domain.pay.dto.CompletePayRequest;
import com.loopmarket.domain.pay.dto.CompletePayResponse;
import com.loopmarket.domain.pay.dto.RefundRequest;
import com.loopmarket.domain.pay.dto.RefundResponse;
import com.loopmarket.domain.pay.dto.SafePayRequest;
import com.loopmarket.domain.pay.dto.SafePayResponse;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.service.PayService;

import lombok.RequiredArgsConstructor;

/**
 * 페이 관련 API 전용 컨트롤러 REST 방식으로 /api/pay/charge 같은 AJAX 요청 처리
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayApiController {

	// @RequiredArgsConstructor로 PayService를 주입받아 충전 관련 로직을 위임
	private final PayService payService;

	/**
	 * 포트원 결제 성공 이후 호출되는 충전 처리 API pay.js에서 fetch("/api/pay/charge")로 호출됨
	 */
	@PostMapping("/charge")
	public ResponseEntity<ChargeResponse> charge(@RequestBody ChargeRequest request) {
		// 1. 결제 수단 enum으로 변환
		PaymentMethod method = PaymentMethod.valueOf(request.getMethod());

		// 2. 충전 처리 (잔액 + 거래기록)
		payService.charge(request.getUserId(), request.getAmount(), method);

		// 3. 현재 잔액 조회
		int balance = payService.getBalance(request.getUserId());

		// 4. 응답 반환
		// 추후 ApiResponse<ChargeResponse> 형태의 공통 응답 포맷으로 변경 논의 필요할 것 같습니다.
		ChargeResponse response = new ChargeResponse(true, "충전이 완료되었습니다.", balance);
		return ResponseEntity.ok(response);
	}

	/**
	 * 로그인 사용자의 잔액 조회 API
	 */
	@GetMapping("/balance/{userId}")
	public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long userId) {
		int balance = payService.getBalance(userId);
		BalanceResponse response = new BalanceResponse(true, "현재 잔액 조회 성공", balance);
		return ResponseEntity.ok(response);
	}

	/**
	 * 페이 환불 처리 API
	 */
	@PostMapping("/refund")
	public ResponseEntity<RefundResponse> refund(@RequestBody RefundRequest request) {
		// 1. 환불 처리
		payService.refund(request.getUserId(), request.getAmount());

		// 2. 현재 잔액 조회
		int balance = payService.getBalance(request.getUserId());

		// 3. 응답 객체 생성
		RefundResponse response = new RefundResponse(true, "환불이 완료되었습니다.", balance);
		return ResponseEntity.ok(response);
	}
	
	/**
	 * 안전결제 요청 API
	 * 구매자가 결제 → 금액 차감 + 거래 기록 + 보류 상태 저장
	 */
	@PostMapping("/safe")
	public ResponseEntity<SafePayResponse> safePay(@RequestBody SafePayRequest request) {
	    Long paymentId = payService.safePay(request.getBuyerId(), request.getProductId());

	    SafePayResponse response = new SafePayResponse(true, "안전결제 요청이 완료되었습니다.", paymentId);
	    return ResponseEntity.ok(response);
	}
	
	/**
	 * 구매 확정 API
	 * 구매자가 확정 → 판매자에게 금액 정산 + 상태 COMPLETED로 변경
	 */
	@PostMapping("/complete")
	public ResponseEntity<CompletePayResponse> completePay(@RequestBody CompletePayRequest request) {
	    int sellerBalance = payService.completePay(request.getPaymentId());

	    CompletePayResponse response = new CompletePayResponse(true, "구매 확정이 완료되었습니다.", sellerBalance);
	    return ResponseEntity.ok(response);
	}
}
