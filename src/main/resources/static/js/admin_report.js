$(document).ready(function () {
    if (window.adminReportScriptInitialized) return;
    window.adminReportScriptInitialized = true;

    const container = $('#admin-content');
    let loadingInterval = null;

    function showLoading() {
        container.html(`<div class="text-center mt-5"><p id="loading-text">Loading.</p></div>`);
        let dotCount = 1;
        loadingInterval = setInterval(() => {
            dotCount = (dotCount % 3) + 1;
            $('#loading-text').text('Loading' + '.'.repeat(dotCount));
        }, 500);
    }

    function stopLoading() {
        clearInterval(loadingInterval);
        loadingInterval = null;
    }

    loadReportFragment();

    // 상태 토글 버튼
    container.on('click', '.toggle-process-btn', function () {
        const reportId = $(this).data('id');
        showLoading();
        fetch(`/admin/reports/${reportId}/toggle`, {
            method: 'POST',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => {
            if (!res.ok) throw new Error('Toggle failed');
            return loadReportFragment();
        })
        .catch(err => {
            console.error(err);
            stopLoading();
            alert('처리 중 오류가 발생했습니다.');
        });
    });

    // 답변 작성 버튼
    container.on('click', 'a.btn-outline-primary', function (e) {
        e.preventDefault();
        const url = $(this).attr('href');
        showLoading();
        fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => updateFragment(html))
        .catch(err => {
            console.error(err);
            stopLoading();
            alert('페이지 로딩 실패');
        });
    });

    // 페이징 AJAX
    container.on('click', '.pagination a', function (e) {
        e.preventDefault();
        const url = $(this).attr('href');
        showLoading();
        fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => updateFragment(html))
        .catch(err => {
            console.error(err);
            stopLoading();
            alert('페이지 로딩 실패');
        });
    });

    // 신고 리스트 또는 답변 폼 fragment 불러오기
    function loadReportFragment() {
        showLoading();
        return fetch('/admin/reports', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => updateFragment(html))
        .catch(err => {
            console.error(err);
            stopLoading();
            alert('데이터 로드 실패');
        });
    }

    // 공통 fragment 업데이트 함수
    function updateFragment(html) {
        const dom = new DOMParser().parseFromString(html, 'text/html');
        const fragmentContent = dom.querySelector('#admin-content');
        if (fragmentContent) {
            container.html(fragmentContent.innerHTML);
        } else {
            container.html(html);
        }
        stopLoading();
        bindReplyForm();
    }

    container.on('click', 'a.ajax-page-link', function(e) {
        e.preventDefault();
        const url = $(this).data('url') || $(this).attr('href');
        if (!url || url === '#') return;

        showLoading();
        fetch(url, {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => {
            const dom = new DOMParser().parseFromString(html, 'text/html');
            const fragmentContent = dom.querySelector('#admin-content');
            if (fragmentContent) {
                container.html(fragmentContent.innerHTML);
            } else {
                container.html(html);
            }
            stopLoading();
            bindReplyForm();
        })
        .catch(err => {
            stopLoading();
            console.error("페이지 로딩 실패:", err);
            alert('페이지 로딩 중 오류가 발생했습니다.');
        });
    });

    // 답변 폼 제출 처리 함수
    function bindReplyForm() {
        container.find('#reportReplyForm').off('submit').on('submit', function (e) {
            e.preventDefault();
            const form = $(this);
            showLoading();
            $.ajax({
                url: form.attr('action'),
                method: 'POST',
                data: form.serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                dataType: 'json'
            })
            .done((response) => {
                if (response.status === 'success') {
                    alert('답변이 등록되었습니다.');
                    loadReportFragment();
                } else {
                    stopLoading();
                    alert('답변 등록 실패: ' + (response.message || '알 수 없는 오류'));
                }
            })
            .fail(() => {
                stopLoading();
                alert('답변 등록 실패 - 서버 오류');
            });
        });
    }
});