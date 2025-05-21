// category.js

document.addEventListener("DOMContentLoaded", () => {
  const mainSelect = document.getElementById("main-category");
  const subSelect = document.getElementById("sub-category");

  if (!mainSelect || !subSelect) return; // 안전장치

  mainSelect.addEventListener("change", () => {
    const mainCode = mainSelect.value;

    if (!mainCode) {
      subSelect.innerHTML = '<option value="">소분류 선택</option>';
      return;
    }

    fetch(`/api/categories/${mainCode}`)
      .then(res => res.json())
      .then(data => {
        subSelect.innerHTML = '<option value="">소분류 선택</option>';
        data.forEach(ctg => {
          subSelect.innerHTML += `<option value="${ctg.ctgCode}">${ctg.ctgName}</option>`;
        });
      });
  });
});
