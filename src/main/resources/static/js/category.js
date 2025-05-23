// DOM이 모두 로드된 후 실행
document.addEventListener("DOMContentLoaded", () => {
  const mainSelect = document.getElementById("main-category");  // 대분류 select 박스
  const subSelect = document.getElementById("sub-category");    // 소분류 select 박스

  if (!mainSelect || !subSelect) return; // 두 요소가 없으면 실행 중단 (안전장치)

  // 대분류 선택값이 바뀔 때마다 실행
  mainSelect.addEventListener("change", () => {
    const mainCode = mainSelect.value;  // 선택된 대분류 코드

    if (!mainCode) {
      // 대분류 선택 안 하면 소분류 초기화
      subSelect.innerHTML = '<option value="">소분류 선택</option>';
      return;
    }

    // 서버에 선택한 대분류 코드로 소분류 목록 요청
    fetch(`/api/categories/${mainCode}`)
      .then(res => res.json()) // JSON 응답 파싱
      .then(data => {
        // 소분류 select 박스 초기화
        subSelect.innerHTML = '<option value="">소분류 선택</option>';

        // 받아온 소분류 목록을 option 태그로 추가
        data.forEach(ctg => {
          subSelect.innerHTML += `<option value="${ctg.ctgCode}">${ctg.ctgName}</option>`;
        });
      });
  });
});
