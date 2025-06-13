$(document).ready(function () {
  const isMobile = window.innerWidth <= 576;

  const tabRoutes = {
    'tab-selling-products': '/mypage/selling-products',
    'tab-wishlist': '/mypage/wishlist',
    'tab-purchase-history': '/mypage/purchase-history',
    'tab-sales-history': '/mypage/sales-history',
    'tab-report-history': '/mypage/report-history'
  };

  // PC 탭 첫 로딩 처리
  if (!isMobile && $('#mypageTabs').length > 0) {
    loadTabContent(tabRoutes['tab-selling-products'], $('#tab-selling-products')[0]);
  }

  // PC 탭 클릭 시 AJAX 로딩
  $('#mypageTabs .nav-link').on('click', function (event) {
    const tabId = this.id;
    const url = tabRoutes[tabId];
    if (!url) return;

    if (isMobile) return; // 모바일은 전체 페이지 이동

    event.preventDefault();
    loadTabContent(url, this);
  });

  // AJAX 콘텐츠 로딩 함수
  function loadTabContent(url, tab) {
    $('#mypageTabs .nav-link').removeClass('active');
    $(tab).addClass('active');

    $('#mypage-content').load(url, function (response, status, xhr) {
      if (status === 'error') {
        $('#mypage-content').html("<div class='text-danger text-center py-4'>콘텐츠를 불러오는 데 실패했습니다.</div>");
      } else {
        bindEventsByPath(url);
      }
    });
  }
  
  // 삭제 기능
  function bindDeleteProductEvents() {
    $('.btn-delete-product').off('click').on('click', function () {
      const productId = $(this).data('id');
      Swal.fire({
        title: '정말 이 상품을 삭제하시겠습니까?',
        text: '삭제된 상품은 복구할 수 없습니다.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#aaa',
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
      }).then((result) => {
        if (result.isConfirmed) {
          $.ajax({
            url: `/mypage/api/products/${productId}`,
            type: 'DELETE',
            headers: { 'X-Requested-With': 'XMLHttpRequest' },
            success: function () {
              Swal.fire({
                icon: 'success',
                title: '삭제 완료!',
                text: '상품이 삭제되었습니다.',
                timer: 1200,
                showConfirmButton: false
              }).then(() => location.reload()); // 또는 해당 row 제거
            },
            error: function () {
              Swal.fire({
                icon: 'error',
                title: '삭제 실패',
                text: '상품 삭제에 실패했습니다. 다시 시도해주세요.'
              });
            }
          });
        }
      });
    });
  }

  // 공통: 상태 변경
  function bindStatusChangeEvents() {
    $('select[data-status-change]').off('change').on('change', function () {
      const productId = $(this).data('id');
      const newStatus = $(this).val();

      $.ajax({
        url: `/api/products/${productId}/status`,
        type: 'PATCH',
        contentType: 'application/json',
        data: JSON.stringify({ status: newStatus }),
        success: function () {
          Swal.fire({
            icon: 'success',
            title: '상태 변경 완료!',
            text: `상품 상태가 '${convertStatusText(newStatus)}'으로 변경되었습니다.`,
            timer: 1500,
            showConfirmButton: false
          });
        },
        error: function () {
          Swal.fire({
            icon: 'error',
            title: '변경 실패',
            text: '상품 상태 변경 중 오류가 발생했습니다.'
          });
        }
      });
    });
  }

  // 공통: 숨김 처리 및 해제 버튼
  function bindHideToggleEvents() {
    $('.btn-toggle-hide').off('click').on('click', function () {
      const productId = $(this).data('id');
      const isCurrentlyHidden = $(this).text().trim() === '숨김 해제';

      $.ajax({
        url: `/api/products/${productId}/hide`,
        type: 'PATCH',
        success: function () {
          Swal.fire({
            icon: 'success',
            title: isCurrentlyHidden ? '숨김 해제 완료' : '숨김 처리 완료',
            text: isCurrentlyHidden ? '상품이 공개되었습니다.' : '상품이 숨김 처리되었습니다.',
            timer: 1000,
            showConfirmButton: false
          }).then(() => {
            location.reload();
          });
        },
        error: function () {
          Swal.fire({
            icon: 'error',
            title: '처리 실패',
            text: '서버 오류가 발생했습니다.'
          });
        }
      });
    });
  }

  // 공통: 찜 해제 버튼
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
        if (result.isConfirmed) deleteWishlist(productId);
      });
    });
  }

  function deleteWishlist(productId) {
    $.ajax({
      url: `/api/wishlist/${productId}`,
      type: 'DELETE',
      success: function () {
        location.reload();
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

  function convertStatusText(status) {
    switch (status) {
      case 'ONSALE': return '판매중';
      case 'RESERVED': return '예약중';
      default: return status;
    }
  }

  // 공통 함수: 경로에 따라 필요한 이벤트 바인딩
  function bindEventsByPath(pathname) {
    if (pathname.includes('/mypage/selling-products')) {
      bindStatusChangeEvents();
      bindHideToggleEvents();
	  bindDeleteProductEvents();
    } else if (pathname.includes('/mypage/wishlist')) {
      bindWishlistRemoveEvents();
    }
  }

  // 전체 페이지 이동으로 접근한 경우에도 이벤트 바인딩 (모바일 포함)
  bindEventsByPath(window.location.pathname);
});