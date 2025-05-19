function render(viewName, callback) {
    fetch(`/${viewName}`)
        .then(res => {
            if (!res.ok) throw new Error("404 Not Found");
            return res.text();
        })
        .then(html => {
            document.getElementById("main-content").innerHTML = html;
            if (callback) callback();
        })
        .catch(err => {
            document.getElementById("main-content").innerHTML = "<p>페이지를 불러오지 못했습니다.</p>";
            console.error(err);
        });
}