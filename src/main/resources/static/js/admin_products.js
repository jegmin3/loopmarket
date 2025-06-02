$(document).ready(function () {
  let currentPage = 0;
  const pageSize = 10;
  let loading = false;

  function loadProducts() {
    if (loading) return;
    loading = true;

    $.ajax({
      url: `/admin/api/products/paged?page=${currentPage}&size=${pageSize}`,
      method: 'GET',
	  dataType: 'json',  // ✅ 반드시 명시
      success: function(response) {
        console.log(response);  // 응답 구조 꼭 확인

        const products = response.content;

        if (!products || products.length === 0) {
          $(window).off('scroll', onScroll);
          loading = false;
          return;
        }

		// 상품 행 추가 예시, 상태명과 카테고리명을 변환
		products.forEach(product => {
		  const categoryName = getCategoryName(product.ctgCode);
		  const statusName = getStatusName(product.status);

		  const actionBtn = product.isHidden
		    ? `<button class="btn btn-success btn-sm" onclick="toggleVisibility('${product.productId}', false)">해제</button>`
		    : `<button class="btn btn-warning btn-sm" onclick="toggleVisibility('${product.productId}', true)">숨기기</button>`;

		  $('#product-tbody').append(`
		    <tr data-id="${product.productId}">
		      <td>${product.title}</td>
		      <td>${product.price}</td>
		      <td>${categoryName}</td>
		      <td>${statusName}</td>
		      <td>
		        ${actionBtn}
		        <button class="btn btn-danger btn-sm" onclick="deleteProduct('${product.productId}')">삭제</button>
		      </td>
		    </tr>
		  `);
		});

        currentPage++;
        loading = false;
      },
      error: function() {
        loading = false;
        alert('상품 로드 중 오류 발생');
      }
    });
  }

  window.toggleVisibility = function(productId, hide) {
    const url = hide
      ? `/admin/api/products/${productId}/hide`
      : `/admin/api/products/${productId}/unhide`;

    $.post(url)
      .done(() => {
        alert(hide ? '상품이 숨김 처리되었습니다.' : '숨김이 해제되었습니다.');
        location.reload();
      })
      .fail(() => alert('처리 실패'));
  }

  window.deleteProduct = function(productId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    $.ajax({
      url: `/admin/api/products/${productId}`,
      type: 'DELETE',
      success: function() {
        alert('상품이 삭제되었습니다.');
        location.reload();
      },
      error: function() {
        alert('삭제 실패');
      }
    });
  }

  function onScroll() {
    if ($(window).scrollTop() + $(window).height() > $(document).height() - 100) {
      loadProducts();
    }
  }

  loadProducts();
  $(window).on('scroll', onScroll);
});