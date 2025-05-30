$(document).ready(function () {
	// 기본 탭 로드
	loadTabContent('/mypage/products', $('#tab-selling-products')[0]);

	// 탭 ID와 URL 매핑
	const tabRoutes = {
		'tab-selling-products': '/mypage/products',
		'tab-wishlist': '/mypage/wishlist',
		'tab-purchase-history': '/mypage/purchase',
		'tab-sales-history': '/mypage/sales'
	};

	// 탭 클릭 핸들링 (공통화)
	$('#mypageTabs .nav-link').on('click', function (event) {
		event.preventDefault();

		const url = tabRoutes[this.id];
		if (url) {
			loadTabContent(url, this);
		}
	});

	// 탭 콘텐츠 로딩 함수
	function loadTabContent(url, tab) {
		$('#mypageTabs .nav-link').removeClass('active');
		$(tab).addClass('active');

		$('#mypage-content').load(url, function (response, status, xhr) {
			if (status === 'error') {
				console.error('탭 콘텐츠 로드 실패:', xhr.status, xhr.statusText);
				$('#mypage-content').html("<div class='text-danger text-center py-4'>콘텐츠를 불러오는 데 실패했습니다.</div>");
			}
		});
	}

	// 잔액 API 호출하여 #pay-balance에 표시
	fetch('/api/pay/balance')
		.then(res => res.json())
		.then(data => {
			if (data.success) {
				const amount = data.data.amount;
				document.getElementById('pay-balance').innerText = `${amount.toLocaleString()}원`;
			} else {
				document.getElementById('pay-balance').innerText = '잔액 조회 실패';
			}
		})
		.catch(err => {
			console.error('잔액 조회 오류', err);
			document.getElementById('pay-balance').innerText = '오류';
		});
});