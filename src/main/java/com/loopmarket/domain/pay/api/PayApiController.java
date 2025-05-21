package com.loopmarket.domain.pay.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopmarket.domain.pay.dto.ChargeRequest;
import com.loopmarket.domain.pay.dto.ChargeResponse;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.service.PayService;

import lombok.RequiredArgsConstructor;

/**
 * 페이 관련 API 전용 컨트롤러
 * REST 방식으로 /api/pay/charge 같은 AJAX 요청 처리
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayApiController {
	
	//@RequiredArgsConstructor로 PayService를 주입받아 충전 관련 로직을 위임
	private final PayService payService;
	
	/**
	 * 포트원 결제 성공 이후 호출되는 충전 처리 API
	 * 프론트에서 fetch("/api/pay/charge")로 호출됨
	 */
	@PostMapping("/charge")
	public ResponseEntity<ChargeResponse> charge(@RequestBody ChargeRequest request){
		// 1. 결제 수단 enum으로 변환
		PaymentMethod method = PaymentMethod.valueOf(request.getMethod());
		
		// 2. 충전 처리 (잔액 + 거래기록)
		payService.charge(request.getUserId(), request.getAmount(), method);
		
		// 3. 현재 잔액 조회
		int currentBalance = payService.getCurrentBalance(request.getUserId());
		
		// 4. 응답 반환
		//    추후 ApiResponse<ChargeResponse> 형태의 공통 응답 포맷으로 변경 논의 필요할 것 같습니다.
		ChargeResponse response = new ChargeResponse(
			true,
			"충전이 완료되었습니다.",
			currentBalance
		);
		return ResponseEntity.ok(response);
	}
}
