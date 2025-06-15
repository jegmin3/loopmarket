function bindUserStatusFormEvents() {
    document.querySelectorAll('.user-status-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const formData = new FormData(form);
            const page = form.querySelector('input[name="page"]').value;

            fetch(form.getAttribute('action'), {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        Swal.fire({
                            icon: 'success',
                            title: '상태 변경 성공',
                            text: data.message,
                            timer: 1500,
                            showConfirmButton: false
                        }).then(() => {
                            loadAdminPage('/admin/user?page=' + page);
                        });
                    } else {
                        Swal.fire({ icon: 'error', title: '실패', text: data.message });
                    }
                })
                .catch(err => {
                    console.error('상태 변경 에러:', err);
                    Swal.fire({ icon: 'error', title: '에러 발생', text: '잠시 후 다시 시도해주세요.' });
                });
        });
    });
}

function bindUserRoleFormEvents() {
    document.querySelectorAll('.user-role-form').forEach(form => {
        form.addEventListener('submit', function (e) {
            e.preventDefault();
            const formData = new FormData(form);
            const page = form.querySelector('input[name="page"]').value;

            fetch(form.getAttribute('action'), {
                method: 'POST',
                body: formData,
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        Swal.fire({
                            icon: 'success',
                            title: '권한 변경 성공',
                            text: data.message,
                            timer: 1500,
                            showConfirmButton: false
                        }).then(() => {
                            loadAdminPage('/admin/user?page=' + page);
                        });
                    } else {
                        Swal.fire({ icon: 'error', title: '실패', text: data.message });
                    }
                })
                .catch(err => {
                    console.error('권한 변경 에러:', err);
                    Swal.fire({ icon: 'error', title: '에러 발생', text: '잠시 후 다시 시도해주세요.' });
                });
        });
    });
}

function bindPaginationEvents() {
    document.querySelectorAll('.user-page-link').forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const page = this.getAttribute('data-page');
            loadAdminPage('/admin/user?page=' + page);
        });
    });
}

// 자동 실행
bindUserStatusFormEvents();
bindUserRoleFormEvents();
bindPaginationEvents();