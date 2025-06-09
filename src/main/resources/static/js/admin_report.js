$(document).ready(function () {
	
	console.log('document ready 실행됨1111111111111111');
	
    // 중복 실행 방지
    if (window.adminReportScriptInitialized) return;
    window.adminReportScriptInitialized = true;

    const container = $('#admin-content'); // admin.html 콘텐츠 영역

    // 최초 로딩 (리스트 등)
    loadReportFragment();

    // 이벤트 위임 (toggle 버튼)
    container.on('click', 'button[data-id]', function () {
        const reportId = $(this).data('id');
        fetch(`/admin/reports/${reportId}/toggle`, {
            method: 'POST',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(res => {
            if (!res.ok) throw new Error('Toggle failed');
            return loadReportFragment();
        })
        .catch(console.error);
    });

    // 여기 a.btn-outline-primary 클릭 이벤트도 추가
    container.on('click', 'a.btn-outline-primary', function(e) {
        e.preventDefault();
        const url = $(this).attr('href');
        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(res => res.text())
        .then(html => {
            const dom = new DOMParser().parseFromString(html, 'text/html');
            const fragmentContent = dom.querySelector('#admin-content');
            container.html(fragmentContent ? fragmentContent.innerHTML : html);
            bindReplyForm();
        });
    });

    // 신고 리스트나 답변 폼 등 fragment 불러오기
    function loadReportFragment() {
		
		console.log('loadReportFragment 호출됨');
		
        return fetch('/admin/reports', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.text())
        .then(html => {
            const dom = new DOMParser().parseFromString(html, 'text/html');
            const fragmentContent = dom.querySelector('#admin-content');

            if (fragmentContent) {
                container.html(fragmentContent.innerHTML);
            } else {
                container.html(dom.body.innerHTML);
            }
			
			console.log('로드 후 #reportReplyForm 존재 여부:', container.find('#reportReplyForm').length);

            // 신고 답변 폼이 새로 로드되면 bindReplyForm 호출
            bindReplyForm();
        });
    }

    // 답변 폼 제출 처리 함수
    function bindReplyForm() {
		console.log('bindReplyForm 호출됨');

		container.find('#reportReplyForm').off('submit').on('submit', function (e) {
		    e.preventDefault();
		    console.log('폼 submit 이벤트 실행됨');
            const form = $(this);
            console.log('답변 제출 시도:', form.serialize());
            $.ajax({
                url: form.attr('action'),
                method: 'POST',
                data: form.serialize(),
                headers: { 'X-Requested-With': 'XMLHttpRequest' },
                dataType: 'json'  // JSON으로 파싱
            })
            .done((response) => {
                console.log("서버 응답:", response);
				if (response.status === 'success') {
				    alert('답변이 등록되었습니다.');
				    // 전체 페이지 리로드 대신 fragment만 새로 로드
				    loadReportFragment();
				}
            })
            .fail(() => {
                alert('답변 등록 실패 - 서버 오류');
            });
        });
    }
});