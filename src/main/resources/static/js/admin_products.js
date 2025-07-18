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
  
  function renderProductCards(products) {
    const container = $('#product-card-list');
    container.empty();

    if (products.length === 0) {
      container.html('<p>상품이 없습니다.</p>');
      return;
    }

    products.forEach(product => {
      const categoryName = getCategoryName(product.ctgCode);
      const statusName = getStatusName(product.status, product.ishidden);
      const thumbnail = product.thumbnailUrl || '/img.pay/kakao.png';
      const isHidden = product.ishidden;
      const buttonLabel = isHidden ? '🔓 공개하기' : '🔒 숨기기';
      const buttonClass = isHidden ? 'btn btn-sm btn-success' : 'btn btn-sm btn-dark';
      const safeTitle = product.title.replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

      const cardHtml = `
        <div class="product-card" style="
          display: flex;
          flex-wrap: wrap;
          align-items: center;
          gap: 12px;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
          margin-bottom: 15px;
          background-color: #fff;
          box-shadow: 0 2px 4px rgba(0,0,0,0.1);
          width: 100%;
        ">
          <img src="${thumbnail}" alt="${safeTitle}" style="width:80px; height:auto; border-radius:6px; flex-shrink: 0;">
          <div style="flex: 1; font-size: 0.95rem; min-width: 200px;">
            <div><strong>${safeTitle}</strong></div>
            <div>가격: ${(product.price ?? 0).toLocaleString()}원</div>
            <div>카테고리: ${categoryName}</div>
            <div>상태: ${statusName}</div>
          </div>
          <div style="
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
            justify-content: flex-end;
            flex: 1;
            min-width: 140px;
          ">
            <button class="btn btn-sm btn-danger" style="flex: 1 1 70px;" onclick="deleteProduct('${product.productId}')">삭제</button>
            <button class="${buttonClass}" style="flex: 1 1 90px;" onclick="toggleHide('${product.productId}', ${isHidden})">${buttonLabel}</button>
          </div>
        </div>
      `;

      container.append(cardHtml);
    });
  }
  
  function goToPage(pageNumber) {
    loadProducts(pageNumber);

    setTimeout(() => {
      const content = document.getElementById('admin-content');
      if (content) {
        content.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }, 100);
  }
  

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
    console.log(`📥 loadProducts(${page}) 호출됨`);
    currentPage = page;

    $.ajax({
      url: '/admin/api/products/paged',
      data: { page: currentPage, size: pageSize },
      method: 'GET'
    }).done(response => {
      console.log("📦 전체 응답:", response);
      console.log("📄 totalPages:", response.totalPages);

      const products = response.content || [];
      totalPages = response.totalPages || 1;

      const isMobile = window.innerWidth < 768;
      const tbody = $('#product-tbody');
      const cardList = $('#product-card-list');

      // 먼저 양쪽 모두 비움
      tbody.empty();
      cardList.empty();

      if (products.length === 0) {
        if (isMobile) {
          cardList.html('<p>상품이 없습니다.</p>');
        } else {
          tbody.html('<tr><td colspan="5">상품이 없습니다.</td></tr>');
        }
      } else {
        if (isMobile) {
          renderProductCards(products); // 모바일 카드만 렌더링
        } else {
          // PC 테이블 렌더링
          products.forEach(product => {
            const categoryName = getCategoryName(product.ctgCode);
            const statusName = getStatusName(product.status, product.ishidden);
            const thumbnail = product.thumbnailUrl || '/img.pay/kakao.png';
            const isHidden = product.ishidden;
            const buttonLabel = isHidden ? '🔓 공개하기' : '🔒 숨기기';
            const buttonClass = isHidden ? 'btn btn-sm btn-success' : 'btn btn-sm btn-dark';
            const rowClass = isHidden ? 'table-secondary' : '';
            const buttonTitle = isHidden ? '숨긴 상품을 다시 보이게 합니다' : '상품을 숨깁니다';

            const row = `
              <tr class="${rowClass}">
                <td class="text-start">
                  <img src="${thumbnail}" style="width:50px; height:auto; margin-right:5px; vertical-align: middle;">
                  ${product.title}
                </td>
                <td>${(product.price ?? 0).toLocaleString()}원</td>
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
    if (currentPage > 0) goToPage(currentPage - 1);
  });

  $('#nextPageBtn').click(() => {
    if (currentPage < totalPages - 1) goToPage(currentPage + 1);
  });

  // refreshCategoryList();  // 카테고리 목록 UI 채우기
  // 초기 실행 흐름
  loadCategories().then(() => {
    loadProducts(0);
  });
});