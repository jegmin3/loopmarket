$(document).ready(function () {
	function buildCategoryTree(categories) {
	  const map = {};
	  const roots = [];

	  categories.forEach(cat => {
	    cat.children = [];
	    map[cat.ctgCode] = cat;
	  });

	  categories.forEach(cat => {
	    // upCtgCode가 null 또는 undefined일 경우 대분류로 판단
	    if (cat.upCtgCode === null || cat.upCtgCode === undefined) {
	      roots.push(cat);
	    } else {
	      const parent = map[cat.upCtgCode];
	      if (parent) {
	        parent.children.push(cat);
	      } else {
	        // 부모가 없는 경우도 대분류로 처리하거나 에러 로깅 가능
	        roots.push(cat);
	        console.warn(`부모 카테고리를 찾을 수 없음: ${cat.ctgCode} -> ${cat.upCtgCode}`);
	      }
	    }
	  });

	  return roots;
	}

	function populateParentCategorySelect(categories) {
	  const $select = $('#parentCategorySelect');
	  $select.empty();
	  $select.append('<option value="" disabled selected>-- 상위 카테고리 선택 --</option>');

	  categories.forEach(cat => {
	    if (cat.upCtgCode === null || cat.upCtgCode === undefined) {
	      $select.append(`<option value="${cat.ctgCode}">${cat.ctgName}</option>`);
	    }
	  });
	}
	
/*	function renderCategoryTree(categories, $container) {
	  categories.forEach(cat => {
	    const canDelete = cat.productCount === 0;
	    const $li = $(`
	      <li class="list-group-item d-flex justify-content-between align-items-center">
	        <span>${cat.ctgName}</span>
	        ${canDelete ? `<button class="btn btn-sm btn-danger">삭제</button>` : `<span class="badge bg-secondary">사용중</span>`}
	      </li>
	    `);

	    if (canDelete) {
	      $li.find('button').click(() => deleteCategory(cat.ctgCode));
	    }

	    $container.append($li);

	    if (cat.children.length > 0) {
	      const $subUl = $('<ul class="list-group ms-3 mt-2"></ul>');  // 약간 들여쓰기
	      $li.append($subUl);
	      renderCategoryTree(cat.children, $subUl);
	    }
	  });
	}*/
	function renderCategoryTree(categories, $container) {
	  $container.empty();

	  categories.forEach(cat => {
	    const hasChildCategories = cat.children && cat.children.length > 0;
	    const canDelete = cat.productCount === 0 && !hasChildCategories;

	    const $card = $(`
	      <div class="card mb-3 shadow-sm">
	        <div class="card-header d-flex justify-content-between align-items-center toggle-header" style="cursor: pointer;">
	          <div class="d-flex align-items-center gap-2">
	            <span class="toggle-icon">▶</span>
	            <strong>${cat.ctgName}</strong>
	          </div>
	          ${
	            canDelete
	              ? `<button class="btn btn-sm btn-outline-danger fw-bold btn-delete-root">🗑 대분류 삭제</button>`
	              : hasChildCategories
	                ? `<span class="badge bg-warning text-dark">하위 카테고리 존재</span>`
	                : `<span class="badge bg-secondary">사용중</span>`
	          }
	        </div>
	        <ul class="list-group list-group-flush ms-4 category-children"></ul>
	      </div>
	    `);

	    if (canDelete) {
	      $card.find('.btn-delete-root').click((e) => {
	        e.stopPropagation();
	        deleteCategory(cat.ctgCode);
	      });
	    }

	    const $subUl = $card.find('.category-children');
	    renderSubCategories(cat.children, $subUl);
	    $subUl.hide();

	    // 아이콘 요소 참조
	    const $icon = $card.find('.toggle-icon');

	    $card.find('.toggle-header').click(() => {
	      $subUl.slideToggle(200);
	      const isVisible = $subUl.is(":visible");
	      $icon.text(isVisible ? "▼" : "▶");  // 아이콘 바꾸기
	    });

	    $container.append($card);
	  });
	}
	
	function renderSubCategories(children, $container) {
	  children.forEach(cat => {
	    const canDelete = cat.productCount === 0;
		const $li = $(`
		  <li class="list-group-item d-flex justify-content-between align-items-center">
		    <span>${cat.ctgName}</span>
		    ${
		      canDelete
		        ? `<button class="btn btn-sm btn-light border border-danger text-danger">삭제</button>`
		        : `<span class="badge bg-secondary">사용중</span>`
		    }
		  </li>
		`);
	    if (canDelete) {
	      $li.find('button').click(() => deleteCategory(cat.ctgCode));
	    }

	    $container.append($li);

	    // 재귀적 하위 카테고리 (필요시)
	    if (cat.children.length > 0) {
	      const $nestedUl = $('<ul class="list-group ms-3 mt-2"></ul>');
	      $li.append($nestedUl);
	      renderSubCategories(cat.children, $nestedUl);
	    }
	  });
	}
	

	
	function refreshCategoryList() {
	  $.get('/admin/api/categories/details', function (categories) {
	    const $list = $('#categoryList');
	    $list.empty();
	    const tree = buildCategoryTree(categories);
	    renderCategoryTree(tree, $list);
	    populateParentCategorySelect(categories);
	  });
	}
	
	$('#addRootCategoryBtn').off('click').on('click', () => {
	  const name = $('#newRootCategoryName').val().trim();

	  if (!name) return alert('대분류명을 입력하세요.');

	  $.post('/admin/api/categories', { name })
	    .done(() => {
	      alert('대분류가 추가되었습니다.');
	      $('#newRootCategoryName').val('');
	      refreshCategoryList();
	    })
	    .fail(() => alert('대분류 추가 실패'));
	});

	

	$('#addCategoryBtn').off('click').on('click', () => {
		const name = $('#newCategoryName').val().trim();
		  const upCtgCode = $('#parentCategorySelect').val();

		  if (!name) return alert('카테고리명을 입력하세요.');
		  if (!upCtgCode) return alert('상위 카테고리를 선택하세요.');

		  $.post('/admin/api/categories', { name, upCtgCode })
		    .done(() => {
		      alert('카테고리가 추가되었습니다.');
		      $('#newCategoryName').val('');
		      $('#parentCategorySelect').val('');
		      refreshCategoryList();
		    })
		    .fail(() => alert('카테고리 추가 실패'));
		});

  window.deleteCategory = function (ctgCode) {
    if (!confirm('이 카테고리를 정말 삭제하시겠습니까?')) return;

    $.ajax({
      url: `/admin/api/categories/${ctgCode}`,
      type: 'DELETE'
    }).done(() => {
      alert('삭제되었습니다.');
      refreshCategoryList();
    }).fail(() => {
      alert('삭제 실패');
    });
  };

  refreshCategoryList();
});