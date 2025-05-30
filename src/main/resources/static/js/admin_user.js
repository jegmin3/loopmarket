function bindUserStatusFormEvents() {
    document.querySelectorAll('.user-status-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();


            const formData = new FormData(form);

            fetch(form.getAttribute('action'), {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    Swal.fire({
                        icon: 'success',
                        title: '상태 변경 성공',
                        text: data.message,
                        timer: 1500,
                        showConfirmButton: false
                    }).then(() => {
                        // AJAX로 유저 관리 페이지 다시 로드
                        loadAdminPage('/admin/user');

                    });
                } else {
                    Swal.fire({
                        icon: 'error',
                        title: '실패',
                        text: data.message
                    });
                }
            })
            .catch(error => {
                Swal.fire({
                    icon: 'error',
                    title: '에러 발생',
                    text: '잠시 후 다시 시도해주세요.'
                });
                console.error('오류:', error);
            });
        });
    });

function loadAdminPage(url) {
    fetch(url, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(res => res.text())
    .then(html => {
        document.getElementById('admin-content').innerHTML = html;
        if(url.includes('/admin/user')) {
            bindUserStatusFormEvents(); // 유저 관리 페이지면 이벤트 재바인딩
        }
        // 필요한 경우 다른 페이지별 후처리도 여기에 추가 가능
    })
    .catch(err => {
        console.error('페이지 로드 실패:', err);
        document.getElementById('admin-content').innerHTML = '<p style="color:red;">페이지를 불러오지 못했습니다.</p>';
    });
}

document.addEventListener('DOMContentLoaded', function() {
    // 초기 대시보드 로드
    loadAdminPage('/admin/dashboard');

    // 사이드바 메뉴 클릭 시
    document.querySelectorAll('.admin-link').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const url = this.getAttribute('data-url');
            loadAdminPage(url);
        });
    });
});