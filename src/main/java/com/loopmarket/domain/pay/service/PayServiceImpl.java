package com.loopmarket.domain.pay.service;

import com.loopmarket.domain.alram.AlramDTO;
import com.loopmarket.domain.alram.AlramService;
import com.loopmarket.domain.image.service.ImageService;
import com.loopmarket.domain.pay.dto.ConfirmableItem;
import com.loopmarket.domain.pay.entity.MoneyTransaction;
import com.loopmarket.domain.pay.entity.Payment;
import com.loopmarket.domain.pay.entity.UserMoney;
import com.loopmarket.domain.pay.enums.PaymentMethod;
import com.loopmarket.domain.pay.enums.PaymentStatus;
import com.loopmarket.domain.pay.enums.TransactionStatus;
import com.loopmarket.domain.pay.enums.TransactionType;
import com.loopmarket.domain.pay.repository.MoneyTransactionRepository;
import com.loopmarket.domain.pay.repository.PaymentRepository;
import com.loopmarket.domain.pay.repository.UserMoneyRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.service.ProductService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayServiceImpl implements PayService {

	private final UserMoneyRepository userMoneyRepository;
	private final MoneyTransactionRepository moneyTransactionRepository;
	private final PaymentRepository paymentRepository;
	private final ProductService productService;
	private final ImageService imageService;
	private final AlramService alramService;

	/**
	 * 페이 충전 : 포트원 결제 성공 이후 -> 잔액 증가 + 거래 기록
	 */
	@Transactional
	@Override
	public void charge(Long userId, int amount, PaymentMethod method) {
		// 비정상 금액 충전 방어
		if (amount <= 0 || amount > 1_000_000) {
			throw new IllegalArgumentException("충전 금액은 1원 이상 100만원 이하로만 가능합니다.");
		}

		// 1. 사용자 잔액 조회 또는 신규 생성
		UserMoney userMoney = userMoneyRepository.findById(userId).orElse(new UserMoney(userId, 0));

		// 총 잔액이 천만원을 초과하지 않도록 제한
		int balance = userMoney.getBalance();
		if (balance + amount > 10_000_000) {
			throw new IllegalArgumentException("총 잔액은 천만원을 초과할 수 없습니다.");
		}

		// 2. 잔액 증가 후 저장
		userMoney.charge(amount);
		userMoneyRepository.save(userMoney);

		// 3. 거래 기록 저장
		MoneyTransaction transaction = new MoneyTransaction(userId, TransactionType.CHARGE, amount,
				TransactionStatus.SUCCESS, method);
		moneyTransactionRepository.save(transaction);
	}

	/**
	 * 페이 환불 : 사용자 페이 잔액 환불 요청
	 */
	@Transactional
	@Override
	public void refund(Long userId, int amount) {
		// 1. 사용자 잔액 조회
		UserMoney userMoney = userMoneyRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("해당 사용자의 잔액 정보가 없습니다."));

		// 2. 잔액 차감 후 저장
		userMoney.refund(amount);
		userMoneyRepository.save(userMoney);

		// 3. 거래 기록 저장
		MoneyTransaction transaction = new MoneyTransaction(userId, TransactionType.REFUND, amount,
				TransactionStatus.SUCCESS, PaymentMethod.PAY);
		moneyTransactionRepository.save(transaction);
	}

	/**
	 * 충전/환불 등 잔액이 변경됐을 때 실시간으로 변경된 잔액 출력을 위한 메서드 -> 잔액 조회 API 에서 사용
	 */
	@Override
	@Transactional(readOnly = true)
	public int getBalance(Long userId) {
		return userMoneyRepository.findById(userId).map(UserMoney::getBalance).orElse(0);
	}

	/**
	 * 안전결제 처리
	 */
	@Override
	@Transactional
	public Long safePay(Long buyerId, Long productId) {
		// 1. 상품 정보 조회
		ProductEntity product = productService.getProductById(productId);

		Long sellerId = product.getUserId();
		int amount = product.getPrice();

		// 2. 구매자 잔액 조회 및 차감
		UserMoney userMoney = userMoneyRepository.findById(buyerId)
				.orElseThrow(() -> new IllegalArgumentException("잔액 정보가 존재하지 않습니다."));
		userMoney.refund(amount);

		// 3. 거래 기록 저장 (출금) ← 상품 ID 포함
		MoneyTransaction tx = new MoneyTransaction(buyerId, productId, TransactionType.SAFE_PAY, amount,
				TransactionStatus.SUCCESS, PaymentMethod.PAY);
		moneyTransactionRepository.save(tx);

		// 4. 상품 상태를 SOLD로 변경
		productService.updateProductStatus(productId, "SOLD");

		// 5. 결제 보류 정보 저장
		Payment payment = Payment.create(buyerId, sellerId, productId, amount, tx.getTransactionId());
		paymentRepository.save(payment);
		
		// 알림 전송 (판매자에게: 상품이 결제되어 보류 중임)
		AlramDTO alram = AlramDTO.builder()
		    .userId(sellerId.intValue())   // 알림 받을 사람: 판매자
		    .senderId(buyerId.intValue())  // 구매자 ID
		    .type("PAYMENT")
		    .title("상품이 구매되었습니다")
		    .content("구매자가 상품을 결제했습니다. 구매 확정 시 정산 금액이 입금됩니다.")
		    .url("/mypage") // 클릭 시 이동 경로
		    .build();

		alramService.createAdminAlram(alram);

		return payment.getPaymentId();
	}

	/**
	 * 구매 확정 처리
	 */
	@Override
	@Transactional
	public int completePay(Long paymentId, Long buyerId) {
		// 1. 결제 정보 조회
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

		// 2. 구매자 본인 인증 (보안 체크)
		if (!payment.getBuyerId().equals(buyerId)) {
			throw new SecurityException("구매자 정보가 일치하지 않습니다.");
		}

		// 3. 상태 변경
		payment.complete(); // status → COMPLETED

		// 4. 판매자 잔액 업데이트
		UserMoney sellerMoney = userMoneyRepository.findById(payment.getSellerId())
				.orElse(new UserMoney(payment.getSellerId(), 0));
		sellerMoney.charge(payment.getAmount());
		userMoneyRepository.save(sellerMoney);
		
		// 판매자에게 알림 전송
		AlramDTO alram = AlramDTO.builder()
			    .userId(payment.getSellerId().intValue())
			    .senderId(payment.getBuyerId().intValue())
			    .type("PAYMENT")
			    .title("구매 확정 완료")
			    .content("구매 확정되어 정산 금액이 입금되었습니다.")
			    .url("/mypage")
			    .build();

		alramService.createAdminAlram(alram);

		return sellerMoney.getBalance();
	}

	/**
	 * 특정 구매자(buyerId)가 구매 확정을 진행할 수 있는 결제 목록을 반환
	 */
	@Override
	public List<ConfirmableItem> getConfirmablePayments(Long buyerId) {
		List<Payment> payments = paymentRepository.findByBuyerIdAndStatus(buyerId, PaymentStatus.HOLD);

		return payments.stream().map(payment -> {
			ProductEntity product = productService.getProductById(payment.getProductId());
            product.setThumbnailPath(imageService.getThumbnailPath(product.getProductId()));
			return new ConfirmableItem(product.getProductId(), payment.getPaymentId(), product.getTitle(),
					product.getPrice(), product.getThumbnailPath(), payment.getCreatedAt());
		}).collect(Collectors.toList());
	}

	/**
	 * 즉시결제 처리 구매자의 잔액을 차감하고, 판매자의 잔액을 증가시키며, 거래 기록을 출금/입금으로 각각 저장한 뒤 상품 상태를 SOLD로
	 * 변경한다.
	 *
	 * - 사용처: QR 인식 후 /pay/direct-check → /api/pay/direct - 거래 유형:
	 * TransactionType.BUY_NOW
	 */
	@Override
	@Transactional
	public int directPay(Long buyerId, Long sellerId, Long productId) {
		// 1. 구매자 잔액 조회 및 차감
		UserMoney buyerMoney = userMoneyRepository.findById(buyerId)
				.orElseThrow(() -> new IllegalArgumentException("구매자의 잔액 정보가 없습니다."));

		ProductEntity product = productService.getProductById(productId);
		int amount = product.getPrice();

		// 잔액 부족 시 예외
		if (buyerMoney.getBalance() < amount) {
			throw new IllegalArgumentException("잔액이 부족합니다.");
		}

		buyerMoney.refund(amount); // 내부적으로 잔액 차감
		userMoneyRepository.save(buyerMoney);

		// 2. 거래 기록 저장 (출금) ← 상품 ID 포함
		MoneyTransaction outTx = new MoneyTransaction(buyerId, productId, TransactionType.BUY_NOW, amount,
				TransactionStatus.SUCCESS, PaymentMethod.PAY);
		moneyTransactionRepository.save(outTx);

		// 3. 판매자 잔액 조회 및 충전
		UserMoney sellerMoney = userMoneyRepository.findById(sellerId).orElse(new UserMoney(sellerId, 0));
		sellerMoney.charge(amount);
		userMoneyRepository.save(sellerMoney);

		// 4. 거래 기록 저장 (입금) ← 상품 ID 포함
		MoneyTransaction inTx = new MoneyTransaction(sellerId, productId, TransactionType.BUY_NOW, amount,
				TransactionStatus.SUCCESS, PaymentMethod.PAY);
		moneyTransactionRepository.save(inTx);

		// 5. 상품 상태를 SOLD로 변경
		productService.updateProductStatus(productId, "SOLD");

		// 6. 결제 정보 저장 (즉시결제는 곧바로 COMPLETED)
		Payment payment = Payment.create(buyerId, sellerId, productId, amount, outTx.getTransactionId());
		payment.complete(); // 상태 → COMPLETED
		paymentRepository.save(payment);
		
		// 알림 전송 (즉시결제 완료)
		AlramDTO alram = AlramDTO.builder()
		    .userId(sellerId.intValue())   // 알림 받는 사람: 판매자
		    .senderId(buyerId.intValue())  // 구매자
		    .type("PAYMENT")
		    .title("상품이 결제되었습니다")
		    .content("구매자가 상품을 결제했습니다. 정산 금액이 입금되었습니다.")
		    .url("/mypage")
		    .build();

		alramService.createAdminAlram(alram);

		// 7. 판매자 최종 잔액 반환
		return sellerMoney.getBalance();
	}

	/**
	 * 구매 내역 조회: 결제 완료(HOLD 또는 COMPLETED) 상태인 결제 리스트 반환
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ConfirmableItem> getMyPurchaseHistory(Long buyerId) {
		List<Payment> payments = paymentRepository.findByBuyerIdAndStatusIn(buyerId,
				List.of(PaymentStatus.HOLD, PaymentStatus.COMPLETED));

		return payments.stream().map(payment -> {
			ProductEntity product = productService.getProductById(payment.getProductId());
			product.setThumbnailPath(imageService.getThumbnailPath(product.getProductId()));
			return new ConfirmableItem(product.getProductId(), payment.getPaymentId(), product.getTitle(),
					product.getPrice(), product.getThumbnailPath(), payment.getCreatedAt());
		}).collect(Collectors.toList());
	}
}