/* 공통헤더 알림용 js */
$(document).ready(function () {
  // 서버에서 알림 목록 로딩
  function loadNotifications() {
    $.ajax({
      url: "/api/notifications/unread",
      method: "GET",
      success: function (data) {
        const count = data.count;
        const list = data.notifications;

        // 배지 갱신
        if (count > 0) {
          $("#notifBadge").show().text(count > 9 ? '●' : count);
        } else {
          $("#notifBadge").hide();
        }

        // 알림 목록 렌더링
        if (list.length > 0) {
          const html = list.map(n => `
            <div class="alert alert-light border-bottom d-flex justify-content-between align-items-center small">
              <div class="flex-grow-1">
                <a href="${n.url}" class="alram-link text-decoration-none text-dark" data-id="${n.alramId}">
                  <strong>[${n.title}]</strong><br/>
                  ${n.content}
                </a>
              </div>
              <div class="dropdown ms-2">
                <button class="btn btn-sm btn-light" data-bs-toggle="dropdown">
                  <i class="bi bi-three-dots-vertical"></i>
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                  <li><a class="dropdown-item mark-read-btn" href="#" data-id="${n.alramId}">읽음 처리</a></li>
                  <li><a class="dropdown-item delete-btn" href="#" data-id="${n.alramId}">삭제</a></li>
                </ul>
              </div>
            </div>
          `).join('');
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
      showAlert("success", "처리됨", "읽음으로 표시했어요");
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
      showAlert("success", "삭제됨", "알림을 삭제했어요");
      loadNotifications();
    });
  });

  // 모달 열릴 때마다 알림만 로딩 (읽음처리 안 함!)
  $('#notifModal').on('show.bs.modal', function () {
    loadNotifications();
  });
});
