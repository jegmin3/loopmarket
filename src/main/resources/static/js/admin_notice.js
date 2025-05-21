if (!window.noticeScriptInitialized) {
    window.noticeScriptInitialized = true;

    function loadAdminPage(url) {
        $.ajax({
            url: url,
            method: 'GET',
            success: function (data) {
                $('#admin-content').html(data);
            },
            error: function () {
                alert('페이지 로딩에 실패했습니다.');
            }
        });
    }

    $(document).ready(function () {
        /*$('#createNoticeBtn').click(function () {
            $.ajax({
                url: '/admin/notice/form',
                method: 'GET',
                success: function (data) {
                    $('#admin-content').html(data);
                },
                error: function () {
                    alert("폼을 불러오는 데 실패했습니다.");
                }
            });
        });*/
		
		// 이벤트 위임으로 변경
	    $(document).on('click', '#createNoticeBtn', function () {
	        $.ajax({
	            url: '/admin/notice/form',
	            method: 'GET',
	            success: function (data) {
	                $('#admin-content').html(data);
	            },
	            error: function () {
	                alert("폼을 불러오는 데 실패했습니다.");
	            }
	        });
	    });    
			
		// 저장
		$(document).on('submit', '#saveNoticeForm', function (e) {
            e.preventDefault();
            $('#saveBtn').prop('disabled', true);

            $.ajax({
                type: 'POST',
                url: '/admin/notice/save',
                data: $(this).serialize(),
                success: function () {
                    loadAdminPage('/admin/notice');
                },
                error: function () {
                    alert('공지사항 저장 중 오류가 발생했습니다.');
                },
                complete: function () {
                    $('#saveBtn').prop('disabled', false);
                }
            });
    	});
		// 수정
        $(document).on('click', '.btn-warning', function (e) {
            e.preventDefault();
            const url = $(this).attr('href');

            $.ajax({
                url: url,
                method: 'GET',
                success: function (data) {
                    $('#admin-content').html(data);
                },
                error: function () {
                    alert('수정 폼을 불러오는 데 실패했습니다.');
                }
            });
        });
		// 삭제
        $(document).on('click', '.delete-btn', function () {
            const noticeId = $(this).data('id');

            if (confirm('정말 삭제하시겠습니까?')) {
                $.ajax({
                    type: 'POST',
                    url: '/admin/notice/delete/' + noticeId,
                    success: function (result) {
                        if (result === 'success') {
                            alert('삭제되었습니다.');
                            loadAdminPage('/admin/notice');
                        } else {
                            alert('삭제 중 오류가 발생했습니다.');
                        }
                    },
                    error: function () {
                        alert('삭제 중 오류가 발생했습니다.');
                    }
                });
            }
        });
    });
}