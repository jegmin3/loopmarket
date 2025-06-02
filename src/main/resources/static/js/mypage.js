$(document).ready(function() {
	loadTabContent('/mypage/products', $('#tab-selling-products')[0]);

	const tabRoutes = {
		'tab-selling-products': '/mypage/products',
		'tab-wishlist': '/mypage/wishlist',
		'tab-purchase-history': '/mypage/purchase',
		'tab-sales-history': '/mypage/sales'
	};

	$('#mypageTabs .nav-link').on('click', function(event) {
		event.preventDefault();
		const url = tabRoutes[this.id];
		if (url) loadTabContent(url, this);
	});

	// 콘텐츠 로딩 후 이벤트 바인딩
	function loadTabContent(url, tab) {
		$('#mypageTabs .nav-link').removeClass('active');
		$(tab).addClass('active');

		$('#mypage-content').load(url, function(response, status, xhr) {
			if (status === 'error') {
				console.error('탭 콘텐츠 로드 실패:', xhr.status, xhr.statusText);
				$('#mypage-content').html("<div class='text-danger text-center py-4'>콘텐츠를 불러오는 데 실패했습니다.</div>");
			} else {
				bindWishlistRemoveEvents();
				bindStatusChangeEvents();
				bindHideToggleEvents();
			}
		});
	}

	// 상태 한글 변환
	function convertStatusText(status) {
		switch (status) {
			case 'ONSALE': return '판매중';
			case 'RESERVED': return '예약중';
			default: return status;
		}
	}

	// 상태 변경
	function bindStatusChangeEvents() {
		$('select[data-status-change]').off('change').on('change', function() {
			const productId = $(this).data('id');
			const newStatus = $(this).val();

			$.ajax({
				url: `/api/products/${productId}/status`,
				type: 'PATCH',
				contentType: 'application/json',
				data: JSON.stringify({ status: newStatus }),
				success: function() {
					Swal.fire({
						icon: 'success',
						title: '상태 변경 완료!',
						text: `상품 상태가 '${convertStatusText(newStatus)}'으로 변경되었습니다.`,
						timer: 1500,
						showConfirmButton: false
					});
				},
				error: function() {
					Swal.fire({
						icon: 'error',
						title: '변경 실패',
						text: '상품 상태 변경 중 오류가 발생했습니다.'
					});
				}
			});
		});
	}

	// 숨김 토글 처리
	function bindHideToggleEvents() {
		$('.btn-toggle-hide').off('click').on('click', function () {
			const productId = $(this).data('id');

			$.ajax({
				url: `/api/products/${productId}/hide`,
				type: 'PATCH',
				success: function (responseText) {
					const $row = $(`tr[data-id="${productId}"]`);
					const $select = $row.find('select[data-status-change]');
					const $button = $row.find('.btn-toggle-hide');

					// 상태 반전: 응답 메시지를 기반으로 판별
					const isNowHidden = responseText.includes('숨김 처리됨');

					// 클래스 토글 (회색 배경)
					$row.toggleClass('table-secondary', isNowHidden);

					// select 박스 show/hide
					if (isNowHidden) {
						$select.addClass('d-none');
					} else {
						$select.removeClass('d-none');
					}

					// 버튼 텍스트 갱신
					$button.text(isNowHidden ? '숨김 해제' : '숨김 처리');

					// 알림
					Swal.fire({
						icon: 'success',
						title: isNowHidden ? '숨김 처리 완료!' : '숨김 해제 완료!',
						timer: 1200,
						showConfirmButton: false
					});
				},
				error: function () {
					Swal.fire({
						icon: 'error',
						title: '숨김 처리 실패',
						text: '서버 오류가 발생했습니다.'
					});
				}
			});
		});
	}

	// 찜 해제 이벤트
	function bindWishlistRemoveEvents() {
		$('.btn-remove-wishlist').off('click').on('click', function() {
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
				if (result.isConfirmed) deleteWishlist(productId);
			});
		});
	}

	// 찜 해제 API 호출
	function deleteWishlist(productId) {
		$.ajax({
			url: `/api/wishlist/${productId}`,
			type: 'DELETE',
			success: function() {
				updateWishlistCount(productId);
				Swal.fire({
					icon: 'success',
					title: '찜 해제 완료!',
					text: '상품이 찜 목록에서 제거되었습니다.',
					timer: 1500,
					showConfirmButton: false
				});
			},
			error: function() {
				Swal.fire({
					icon: 'error',
					title: '실패!',
					text: '찜 해제에 실패했습니다. 다시 시도해주세요.'
				});
			}
		});
	}

	// 찜 수 감소 처리
	function updateWishlistCount(productId) {
		$(`tr[data-id="${productId}"]`).remove();
		const $countText = $('h6:contains("찜한 상품 수")');
		const match = $countText.text().match(/찜한 상품 수\s*:\s*(\d+)/);
		if (match) {
			const newCount = parseInt(match[1]) - 1;
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
				$balance.text(`${data.balance.toLocaleString()}원`);
			} else {
				$balance.text('잔액 조회 실패');
			}
		})
		.catch(err => {
			console.error('잔액 조회 오류', err);
			$('#pay-balance').text('오류');
		});
});