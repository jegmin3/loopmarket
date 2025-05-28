document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.user-status-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault(); // 기본 submit 막음

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
                        location.reload(); // sidebar 포함 전체 페이지 새로고침
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
});