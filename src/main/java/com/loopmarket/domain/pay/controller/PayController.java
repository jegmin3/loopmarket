package com.loopmarket.domain.pay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopmarket.common.controller.BaseController;
import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.pay.dto.ConfirmableItem;
import com.loopmarket.domain.pay.dto.DirectPayTokenDto;
import com.loopmarket.domain.pay.enums.PaymentStatus;
import com.loopmarket.domain.pay.repository.PaymentRepository;
import com.loopmarket.domain.pay.service.PayService;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * 결제 관련 화면 요청을 처리하는 컨트롤러입니다.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/pay")
public class PayController extends BaseController {

	private final ProductService productService;
	private final PaymentRepository paymentRepository;
	private final PayService payService;

	// 페이 충전 페이지
	@GetMapping("/charge")
	public String showChargePage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/charge", model);
	}

	// 페이 환불 페이지
	@GetMapping("/refund")
	public String showRefundPage(Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		model.addAttribute("loginUser", loginUser);
		return render("pay/refund", model);
	}

	// 결제(안전결제) 화면 진입
	@GetMapping("/checkout")
	public String showCheckoutPage(@RequestParam("productId") Long productId, Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		ProductEntity product = productService.getProductById(productId);

		// 거래 방식 확인 (안전결제는 배송 상품만 가능)
		if (!Boolean.TRUE.equals(product.getIsDelivery())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "안전결제는 택배거래에서만 가능합니다.");
		}

		// 거래 중(HOLD), 정산 완료(COMPLETED) 상품 차단
		List<PaymentStatus> forbiddenStatuses = List.of(PaymentStatus.HOLD, PaymentStatus.COMPLETED);
		if (paymentRepository.existsByProductIdAndStatusIn(productId, forbiddenStatuses)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미 거래 중이거나 완료된 상품입니다.");
		}

		// 숨김 상품 차단
		if (Boolean.TRUE.equals(product.getIsHidden())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 상품은 더 이상 결제가 불가능합니다.");
		}

		// 썸네일 이미지 경로 주입
		String thumbnailPath = productService.getThumbnailPath(productId);
		product.setThumbnailPath(thumbnailPath);

		String tradeType = "택배거래"; // 어차피 isDelivery=true이므로 고정

		model.addAttribute("loginUser", loginUser);
		model.addAttribute("product", product);
		model.addAttribute("tradeType", tradeType);

		return render("pay/checkout", model);
	}
	
	@GetMapping("/direct-check")
	public String showDirectPayPage(@RequestParam("token") String token, Model model) {
		MemberEntity loginUser = getLoginUser();
		if (loginUser == null)
			return "redirect:/member/login";

		try {
			// 1. Base64 디코딩
			String decodedJson = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);

			// 2. JSON 파싱
			ObjectMapper objectMapper = new ObjectMapper();
			DirectPayTokenDto payload = objectMapper.readValue(decodedJson, DirectPayTokenDto.class);

			// 3. QR 유효성 검사
			OffsetDateTime expiresAt = OffsetDateTime.parse(payload.getExpiresAt());
			if (expiresAt.isBefore(OffsetDateTime.now())) {
				throw new IllegalArgumentException("QR 코드가 만료되었습니다.");
			}

			// 4. 상품 정보 조회
			ProductEntity product = productService.getProductById(payload.getProductId());

			// 직거래 상품인지 확인 (즉시결제는 직거래만 가능)
			if (!Boolean.TRUE.equals(product.getIsDirect())) {
				throw new IllegalArgumentException("즉시결제는 직거래 상품에서만 가능합니다.");
			}

			// 썸네일 이미지 세팅
			String thumbnailPath = productService.getThumbnailPath(product.getProductId());
			product.setThumbnailPath(thumbnailPath);

			// 5. 로그인 사용자 잔액 조회
			int balance = payService.getBalance(loginUser.getUserId().longValue());

			// 6. 모델에 담기
			model.addAttribute("loginUser", loginUser);
			model.addAttribute("product", product);
			model.addAttribute("balance", balance);

			return render("pay/direct-check", model);

		} catch (Exception e) {
			model.addAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "잘못된 QR 코드이거나 만료되었습니다.");
			return render("pay/direct-error", model);
		}
	}

	@GetMapping("/test")
	public String showPayWidgetTestPage(Model model) {
	    MemberEntity loginUser = getLoginUser();
	    if (loginUser == null)
	        return "redirect:/member/login";

	    model.addAttribute("loginUser", loginUser);

	    // QR용: 판매 중인 상품 + 썸네일 주입
	    List<ProductEntity> myProducts = productService.getMyProducts(loginUser.getUserId().longValue());
	    for (ProductEntity product : myProducts) {
	        String thumbnailPath = productService.getThumbnailPath(product.getProductId());
	        product.setThumbnailPath(thumbnailPath);
	    }
	    model.addAttribute("myProducts", myProducts);

	    // 변경된 구매 확정용 confirmables: 상품 정보 + 썸네일 포함 DTO 리스트로 받기
	    List<ConfirmableItem> confirmables = payService.getConfirmablePayments(loginUser.getUserId().longValue());
	    model.addAttribute("confirmables", confirmables);

	    return render("pay/pay-test", model);
	}
}