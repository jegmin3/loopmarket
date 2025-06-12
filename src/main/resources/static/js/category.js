document.addEventListener("DOMContentLoaded", () => {
  const mainList = document.getElementById("main-category-list");
  const subList = document.getElementById("sub-category-list");
  const hiddenInput = document.getElementById("selectedSubCategory");

  if (!mainList || !subList || !hiddenInput) return;

  // 대분류 클릭 이벤트
  mainList.addEventListener("click", e => {
    if (!e.target || !e.target.dataset.code) return;

    // 기존 선택된 대분류 스타일 제거
    [...mainList.children].forEach(li => li.classList.remove("active"));
    e.target.classList.add("active");

    const mainCode = e.target.dataset.code;

    // 소분류 초기화
    subList.innerHTML = `<li class="list-group-item text-muted text-center">로딩 중...</li>`;

    // 대분류에 따라 소분류 리스트 요청
    fetch(`/api/categories/${mainCode}`)
      .then(res => res.json())
      .then(data => {
        if (!data.length) {
          subList.innerHTML = `<li class="list-group-item text-muted text-center">소분류 없음</li>`;
          return;
        }

        // 소분류 리스트 렌더링
        subList.innerHTML = data.map(sub => `
          <li class="list-group-item list-group-item-action" data-code="${sub.ctgCode}">
            ${sub.ctgName}
          </li>`).join('');
      });
  });

  // 소분류 클릭 이벤트
  subList.addEventListener("click", e => {
    if (!e.target || !e.target.dataset.code) return;

    // 기존 선택된 소분류 스타일 제거
    [...subList.children].forEach(li => li.classList.remove("active"));
    e.target.classList.add("active");

    // 최종 선택된 소분류 코드 저장
    hiddenInput.value = e.target.dataset.code;
  });
});
