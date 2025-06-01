$(document).ready(function () {
	loadTabContent('/mypage/products', $('#tab-selling-products')[0]);

	const tabRoutes = {
		'tab-selling-products': '/mypage/products',
		'tab-wishlist': '/mypage/wishlist',
		'tab-purchase-history': '/mypage/purchase',
		'tab-sales-history': '/mypage/sales'
	};

	$('#mypageTabs .nav-link').on('click', function (event) {
		event.preventDefault();

		const url = tabRoutes[this.id];
		if (url) {
			loadTabContent(url, this);
		}
	});

	function loadTabContent(url, tab) {
		$('#mypageTabs .nav-link').removeClass('active');
		$(tab).addClass('active');

		$('#mypage-content').load(url, function (response, status, xhr) {
			if (status === 'error') {
				console.error('탭 콘텐츠 로드 실패:', xhr.status, xhr.statusText);
				$('#mypage-content').html("<div class='text-danger text-center py-4'>콘텐츠를 불러오는 데 실패했습니다.</div>");
			} else {
				bindWishlistRemoveEvents();
			}
		});
	}

	// 찜 해제 버튼 이벤트 바인딩 함수
	function bindWishlistRemoveEvents() {
		$('.btn-remove-wishlist').off('click').on('click', function () {
			const productId = $(this).data('id');

			Swal.fire({
				title: '정말 찜 해제할까요?',
				text: '찜 목록에서 제거됩니다.',
				icon: 'warning',
				showCancelButton: true,
				confirmButtonColor: '#d33',
				cancelButtonColor: '#aaa',
				confirmButtonText: '찜 해제',
				cancelButtonText: '취소'
			}).then((result) => {
				if (result.isConfirmed) {
					deleteWishlist(productId);
				}
			});
		});
	}

	// 찜 해제 API 호출 및 DOM 업데이트
	function deleteWishlist(productId) {
		$.ajax({
			url: `/api/wishlist/${productId}`,
			type: 'DELETE',
			success: function () {
				updateWishlistCount(productId);
				Swal.fire({
					icon: 'success',
					title: '찜 해제 완료!',
					text: '상품이 찜 목록에서 제거되었습니다.',
					timer: 1500,
					showConfirmButton: false
				});
			},
			error: function () {
				Swal.fire({
					icon: 'error',
					title: '실패!',
					text: '찜 해제에 실패했습니다. 다시 시도해주세요.'
				});
			}
		});
	}

	// 찜 수 및 테이블 상태 업데이트
	function updateWishlistCount(productId) {
		$(`tr[data-id="${productId}"]`).remove();

		const $countText = $('h6:contains("찜한 상품 수")');
		const match = $countText.text().match(/찜한 상품 수\s*:\s*(\d+)/);
		if (match) {
			const currentCount = parseInt(match[1]);
			const newCount = currentCount - 1;
			$countText.text(`찜한 상품 수 : ${newCount}`);

			if (newCount === 0) {
				$('table tbody').html(`<tr><td colspan="4" class="text-center text-muted py-4">찜한 상품이 없습니다.</td></tr>`);
			}
		}
	}

	// 잔액 조회
	fetch('/api/pay/balance')
		.then(res => res.json())
		.then(data => {
			const $balance = $('#pay-balance');
			if (data.success) {
				$balance.text(`${data.data.amount.toLocaleString()}원`);
			} else {
				$balance.text('잔액 조회 실패');
			}
		})
		.catch(err => {
			console.error('잔액 조회 오류', err);
			$('#pay-balance').text('오류');
		});
});