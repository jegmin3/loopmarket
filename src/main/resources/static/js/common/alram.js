$(document).ready(function () {
  // 서버에서 알림 수 조회 (비동기)
  function loadNotifications() {
    $.ajax({
      url: "/api/notifications/unread", // Spring에서 제공하는 API 필요함
      method: "GET",
      success: function (data) {
        const count = data.count;
        const list = data.notifications;
		
		// 배지 표시
        if (count > 0) {
          $("#notifBadge").show().text(count > 9 ? '●' : count);
        } else {
          $("#notifBadge").hide();
        }
		
		// 목록 출력
        if (list.length > 0) {
          const html = list.map(n => `<div class="alert alert-light border-bottom small">${n.content}</div>`).join('');
          $("#notifList").html(html);
        } else {
          $("#notifList").html('<p class="text-muted">알림이 없습니다.</p>');
        }
      }
    });
  }
  // 모달 열릴 때마다 알림 불러오기
  $('#notifModal').on('show.bs.modal', function () {
	// 알림 목록 로딩
    loadNotifications();
	// 읽음 처리 요청
    $.ajax({
      url: "/api/alram/mark-read",
      method: "PATCH"
    }).done(function () {
      $("#notifBadge").hide(); // 뱃지 초기화
    });
  });
});
