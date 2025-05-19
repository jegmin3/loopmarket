/* 요청할 뷰 이름(ViewName), 선택적 콜백 함수. HTML 삽입이 끝난 뒤 실행할 함수(callback) */
function render(viewName, callback) {
	/* HTML 조각 요청 */
	fetch(`/${viewName}`)
	    .then(res => { /* 서버가 200OK를 응답하지 않을 시 에러 발생시킴 */
	        if (!res.ok) throw new Error("404 Not Found");
	        return res.text(); /* 정상 응답이면 .text()로 HTML 문자열 가져옴 */
	    })
	    .then(html => { /* 받아온 HTML을 #main-content 영역에 삽입 */
	        document.getElementById("main-content").innerHTML = html;
	        if (callback) callback(); /* 콜백이 있다면 호출 */
	    })
	    .catch(err => {
	        document.getElementById("main-content").innerHTML = "<p>페이지를 불러오지 못했습니다.</p>";
	        console.error(err);
			/* 요청 실패(404, 네트워크 오류 등) 시 에러 메시지를 본문에 출력하고 콘솔에 로깅 */
	    });
}