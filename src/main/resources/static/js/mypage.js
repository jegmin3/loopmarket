$(document).ready(function () {
    // 기본 탭 로드
    $('#mypage-content').load('/mypage/products');

    // 탭 클릭 핸들링
    $('#tab-selling-products').on('click', function () {
        loadTabContent('/mypage/products', this);
    });
    $('#tab-wishlist').on('click', function () {
        loadTabContent('/mypage/wishlist', this);
    });
    $('#tab-purchase-history').on('click', function () {
        loadTabContent('/mypage/purchase', this);
    });
    $('#tab-sales-history').on('click', function () {
        loadTabContent('/mypage/sales', this);
    });

    function loadTabContent(url, tab) {
        $('#mypageTabs .nav-link').removeClass('active');
        $(tab).addClass('active');
        $('#mypage-content').load(url);
    }
});