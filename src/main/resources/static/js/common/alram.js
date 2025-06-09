/* 공통헤더 알림용 js */
$(document).ready(function () {
  // 페이지 진입 시마다 알림 수 조회해서 뱃지 표시
  loadNotifications();
  // 서버에서 알림 목록 로딩
  function loadNotifications() {
    $.ajax({
      url: "/api/notifications/unread",
      method: "GET",
      success: function (data) {
        const count = data.count;
        const list = data.notifications;

        // 배지 갱신. 깜빡임 줄이기 위해 표시여부 비교함
		if (count > 0) {
		  if ($("#notifBadge").is(":hidden") || $("#notifBadge").text() !== String(count)) {
		    $("#notifBadge").show().text(count > 9 ? '●' : count);
		  }
		} else {
		  $("#notifBadge").hide();
		}

        // 알림 목록 렌더링 (url없으면 클릭 안되게 함)
        if (list.length > 0) {
          const html = list.map(n => {
			const isClickable = n.url !== null && n.url !== "";
			
			return `
			  <div class="border-bottom p-3 d-flex justify-content-between align-items-start notif-card">
			    <div class="flex-grow-1 pe-2">
			      <a
			        href="${isClickable ? n.url : '#'}"
			        class="alram-link text-decoration-none text-dark ${!isClickable ? 'disabled-link' : ''}"
			        data-id="${n.alramId}">
			        <div class="fw-bold ${isClickable ? 'text-primary' : 'text-primary'} mb-1">[${n.title}]</div>
			        <div class="small text-muted text-truncate" style="max-width: 100%;">${n.content}</div>
			      </a>
			    </div>
			    <div class="dropdown">
			      <button class="btn btn-sm btn-outline-secondary" data-bs-toggle="dropdown">
			        <i class="bi bi-three-dots-vertical"></i>
			      </button>
			      <ul class="dropdown-menu dropdown-menu-end">
			        <li><a class="dropdown-item mark-read-btn" href="#" data-id="${n.alramId}">읽음 처리</a></li>
			        <li><a class="dropdown-item delete-btn" href="#" data-id="${n.alramId}">삭제</a></li>
			      </ul>
			    </div>
			  </div>
			`;
		  }).join('');
          $("#notifList").html(html);
        } else {
          $("#notifList").html('<p class="text-muted">알림이 없습니다.</p>');
        }
		
      }
    });
  }

  // 알림 클릭 → 읽음 처리 후 페이지 이동
  $(document).on("click", ".alram-link", function (e) {
    e.preventDefault();
    const id = $(this).data("id");
    const url = $(this).attr("href");

    $.ajax({
      url: `/api/alram/${id}/read`,
      type: "PATCH",
      success: () => location.href = url
    });
  });

  // 옵션 - 읽음 처리
  $(document).on("click", ".mark-read-btn", function (e) {
    e.preventDefault();
    const id = $(this).data("id");

    $.ajax({
      url: `/api/alram/${id}/read`,
      type: "PATCH"
    }).done(() => {
      //showAlert("success", "처리됨", "읽음으로 표시했어요");
      loadNotifications();
    });
  });

  // 옵션 - 삭제
  $(document).on("click", ".delete-btn", function (e) {
    e.preventDefault();
    const id = $(this).data("id");

    $.ajax({
      url: `/api/alram/${id}`,
      type: "DELETE"
    }).done(() => {
      //showAlert("success", "삭제됨", "알림을 삭제했어요");
      loadNotifications();
    });
  });
  // 모두읽음 처리
  $(document).on("click", ".mark-all-read", function () {
    Swal.fire({
      title: '전체 읽음 처리',
      text: '모든 알림을 읽음 상태로 변경할까요?',
      icon: 'question',
      showCancelButton: true,
      confirmButtonText: '예, 처리할게요',
      cancelButtonText: '아니요',
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33'
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: "/api/alram/mark-read",
          type: "PATCH"
        }).done(() => {
          showAlert("success", "전체 처리 완료", "모든 알림을 읽음으로 표시했어요");
          loadNotifications(); // 뱃지/목록 새로고침
        });
      }
    });
  });


  // 모달 열릴 때마다 알림만 로딩 (읽음처리 안 함!)
  $('#notifModal').on('show.bs.modal', function () {
    loadNotifications();
  });
});
