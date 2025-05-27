$(document).ready(function() {
	// 기본 탭 로드
	$('#mypage-content').load('/mypage/products');

	// 탭 클릭 핸들링
	$('#tab-selling-products').on('click', function() {
		loadTabContent('/mypage/products', this);
	});
	$('#tab-wishlist').on('click', function() {
		loadTabContent('/mypage/wishlist', this);
	});
	$('#tab-purchase-history').on('click', function() {
		loadTabContent('/mypage/purchase', this);
	});
	$('#tab-sales-history').on('click', function() {
		loadTabContent('/mypage/sales', this);
	});

	function loadTabContent(url, tab) {
		$('#mypageTabs .nav-link').removeClass('active');
		$(tab).addClass('active');
		$('#mypage-content').load(url);
	}

	// 잔액 API 호출하여 #pay-balance에 표시
	fetch('/api/pay/balance')
		.then(res => res.json())
		.then(data => {
			if (data.success) {
				const amount = data.data.amount;
				document.getElementById('pay-balance').innerText = `${amount.toLocaleString()}원`;
			} else {
				document.getElementById('pay-balance').innerText = "잔액 조회 실패";
			}
		})
		.catch(err => {
			console.error("잔액 조회 오류", err);
			document.getElementById('pay-balance').innerText = "오류";
		});
});