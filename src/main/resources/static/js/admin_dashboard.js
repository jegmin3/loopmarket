$(document).ready(function() {
  console.log("jQuery document ready 실행됨");

  fetch('/admin/api/dashboard/today-login-count')
    .then(res => res.json())
    .then(data => {
      console.log("오늘 로그인 수 데이터:", data);
      $('#todayLoginCount').text(data.todayLoginCount + '명');
    })
    .catch(err => console.error("API 호출 실패:", err));
});