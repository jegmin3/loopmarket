package com.loopmarket.domain.pay.api;

import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.pay.dto.*;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.service.PayService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 페이, 결제 관련 API 전용 컨트롤러
 * 
 * REST 방식으로 /api/pay/charge 같은 AJAX 요청 처리
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayApiController extends BaseController {

	private final PayService payService;

	/**
	 * 포트원 결제 성공 이후 호출되는 충전 처리 API
	 * JS에서 fetch("/api/pay/charge")로 호출됨
	 */
	@PostMapping("/charge")
	public ResponseEntity<ChargeResponse> charge(@RequestBody ChargeRequest request) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();
		PaymentMethod method = PaymentMethod.valueOf(request.getMethod());

		payService.charge(userId, request.getAmount(), method);

		int balance = payService.getBalance(userId);
		ChargeResponse response = new ChargeResponse(true, "충전이 완료되었습니다.", balance);
		return ResponseEntity.ok(response);
	}

	/**
	 * 로그인 사용자의 잔액 조회 API
	 */
	@GetMapping("/balance")
	public ResponseEntity<BalanceResponse> getBalance() {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();
		int balance = payService.getBalance(userId);
		BalanceResponse response = new BalanceResponse(true, "현재 잔액 조회 성공", balance);
		return ResponseEntity.ok(response);
	}

	/**
	 * 페이 환불 처리 API
	 */
	@PostMapping("/refund")
	public ResponseEntity<RefundResponse> refund(@RequestBody RefundRequest request) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();
		payService.refund(userId, request.getAmount());

		int balance = payService.getBalance(userId);
		RefundResponse response = new RefundResponse(true, "환불이 완료되었습니다.", balance);
		return ResponseEntity.ok(response);
	}

	/**
	 * 안전결제 요청 API
	 */
	@PostMapping("/safe")
	public ResponseEntity<SafePayResponse> safePay(@RequestBody SafePayRequest request) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();
		Long paymentId = payService.safePay(userId, request.getProductId());

		SafePayResponse response = new SafePayResponse(true, "안전결제 요청이 완료되었습니다.", paymentId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 구매 확정 API
	 */
	@PostMapping("/complete")
	public ResponseEntity<CompletePayResponse> completePay(@RequestBody CompletePayRequest request) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();

		try {
			int sellerBalance = payService.completePay(request.getPaymentId(), userId);
			return ResponseEntity.ok(new CompletePayResponse(true, "구매 확정이 완료되었습니다.", sellerBalance));
		} catch (IllegalArgumentException | SecurityException e) {
			return ResponseEntity.badRequest().body(new CompletePayResponse(false, e.getMessage(), 0));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(new CompletePayResponse(false, "서버 오류가 발생했습니다.", 0));
		}
	}

	/**
	 * 즉시결제 처리 API
	 */
	@PostMapping("/direct")
	public ResponseEntity<DirectPayResponse> directPay(@RequestBody DirectPayRequest request) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null) return ResponseEntity.status(401).build();

		Long userId = loginUser.getUserId().longValue();

		try {
			int balance = payService.directPay(userId, request.getSellerId(), request.getProductId());
			DirectPayResponse response = new DirectPayResponse(true, "즉시결제가 완료되었습니다.", balance);
			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(new DirectPayResponse(false, e.getMessage(), 0));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(new DirectPayResponse(false, "결제 처리 중 오류가 발생했습니다.", 0));
		}
	}
}