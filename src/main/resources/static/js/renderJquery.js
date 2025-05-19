function render(viewName, callback) {
    $.ajax({
        url: `/${viewName}`,
        method: "GET",
        dataType: "html",
        success: function (html) {
            $("#main-content").html(html);
            if (callback) callback();
        },
        error: function () {
            $("#main-content").html("<p>페이지를 불러오지 못했습니다.</p>");
            console.error("페이지 요청 실패:", viewName);
        }
    });
}
