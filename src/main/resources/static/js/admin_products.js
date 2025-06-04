$(document).ready(function () {
  let currentPage = 0;
  const pageSize = 10;
  let loading = false;
  let totalPages = 1;

  const categoryMap = {};
  const statusMap = {
    ONSALE: "판매중",
    SOLD: "판매완료",
    RESERVED: "예약중"
  };

  function getCategoryName(ctgCode) {
    return categoryMap[ctgCode.toString()] || `코드 ${ctgCode}`;
  }

  function getStatusName(status, isHidden) {
    return isHidden ? "숨김" : (statusMap[status] || status);
  }

  // 카테고리 로드
  function loadCategories() {
    return $.ajax({
      url: '/admin/api/categories',
      method: 'GET'
    }).then(categories => {
      categories.forEach(category => {
        categoryMap[category.ctgCode.toString()] = category.ctgName;
      });
    }).catch(err => {
      console.error('❌ 카테고리 불러오기 실패:', err);
      alert('카테고리 정보를 불러올 수 없습니다.');
    });
  }

  // 상품 목록 로드
  function loadProducts(page = 0) {
    if (loading) return;
    loading = true;
    currentPage = page;

    $('#product-tbody').empty();

    $.ajax({
      url: '/admin/api/products/paged',
      data: { page: currentPage, size: pageSize },
      method: 'GET'
    }).done(response => {
      const products = response.content || [];
      totalPages = response.totalPages || 1;

      const tbody = $('#product-tbody');

      if (products.length === 0) {
        tbody.html('<tr><td colspan="5">상품이 없습니다.</td></tr>');
      } else {
        /*products.forEach(product => {
          const categoryName = getCategoryName(product.ctgCode);
          const statusName = getStatusName(product.status, product.ishidden);

          const isHidden = product.ishidden;
          const buttonLabel = isHidden ? '🔓 공개하기' : '🔒 숨기기';
          const buttonClass = isHidden ? 'btn btn-sm btn-success' : 'btn btn-sm btn-dark';
          const rowClass = isHidden ? 'table-secondary' : '';
          const buttonTitle = isHidden ? '숨긴 상품을 다시 보이게 합니다' : '상품을 숨깁니다';

          const row = `
            <tr class="${rowClass}">
              <td>${product.title}</td>
              <td>${product.price.toLocaleString()}원</td>
              <td>${categoryName}</td>
              <td>${statusName}</td>
              <td>
                <button class="btn btn-sm btn-danger" onclick="deleteProduct('${product.productId}')">삭제</button>
                <button class="${buttonClass}" title="${buttonTitle}" onclick="toggleHide('${product.productId}', ${isHidden})">
                  ${buttonLabel}
                </button>
              </td>
            </tr>`;
          tbody.append(row);
        });*/
		
		products.forEach(product => {
		  const categoryName = getCategoryName(product.ctgCode);
		  const statusName = getStatusName(product.status, product.ishidden);
		  const thumbnail = product.thumbnailUrl || '/img.pay/kakao.png';  // 기본 썸네일 이미지

		  const isHidden = product.ishidden;
		  const buttonLabel = isHidden ? '🔓 공개하기' : '🔒 숨기기';
		  const buttonClass = isHidden ? 'btn btn-sm btn-success' : 'btn btn-sm btn-dark';
		  const rowClass = isHidden ? 'table-secondary' : '';
		  const buttonTitle = isHidden ? '숨긴 상품을 다시 보이게 합니다' : '상품을 숨깁니다';

		  const row = `
		    <tr class="${rowClass}">
		      <td>
		        <img src="${thumbnail}" style="width:50px; height:auto; margin-right:5px; vertical-align: middle;">
		        ${product.title}
		      </td>
		      <td>${product.price.toLocaleString()}원</td>
		      <td>${categoryName}</td>
		      <td>${statusName}</td>
		      <td>
		        <button class="btn btn-sm btn-danger" onclick="deleteProduct('${product.productId}')">삭제</button>
		        <button class="${buttonClass}" title="${buttonTitle}" onclick="toggleHide('${product.productId}', ${isHidden})">
		          ${buttonLabel}
		        </button>
		      </td>
		    </tr>`;
		  tbody.append(row);
		});
		
      }

      updatePaginationControls();
    }).fail(() => {
      alert('상품 목록을 불러올 수 없습니다.');
    }).always(() => {
      loading = false;
    });
  }

  // 페이징 버튼 상태 업데이트
  function updatePaginationControls() {
    $('#pageInfo').text(`${currentPage + 1} / ${totalPages}`);
    $('#prevPageBtn').prop('disabled', currentPage <= 0);
    $('#nextPageBtn').prop('disabled', currentPage >= totalPages - 1);
  }

  // 삭제
  window.deleteProduct = function (productId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    $.ajax({
      url: `/admin/api/products/${productId}`,
      type: 'DELETE'
    }).done(() => {
      alert('삭제되었습니다.');
      loadProducts(currentPage); // 현재 페이지 새로고침
    }).fail(() => {
      alert('삭제 실패');
    });
  }

  // 숨김/표시 토글
  window.toggleHide = function (productId, currentlyIsHidden) {
    const action = currentlyIsHidden ? '표시' : '숨김';
    const url = `/admin/api/products/${productId}/${currentlyIsHidden ? 'unhide' : 'hide'}`;

    if (!confirm(`상품을 ${action}하시겠습니까?`)) return;

    $.ajax({
      url: url,
      type: 'POST'
    }).done(() => {
      alert(`상품이 ${action}되었습니다.`);
      loadProducts(currentPage); // 현재 페이지 새로고침
    }).fail(() => {
      alert(`${action} 처리 실패`);
    });
  };

  // 페이징 버튼 클릭 이벤트
  $('#prevPageBtn').click(() => {
    if (currentPage > 0) loadProducts(currentPage - 1);
  });

  $('#nextPageBtn').click(() => {
    if (currentPage < totalPages - 1) loadProducts(currentPage + 1);
  });

  // 초기 실행 흐름
  loadCategories().then(() => {
    loadProducts(0);
  });
});