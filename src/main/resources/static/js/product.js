document.addEventListener("DOMContentLoaded", () => {
  // ---------------- 가격 입력 관련 ----------------
  const priceDisplay = document.getElementById("priceDisplay");
  const priceInput = document.getElementById("priceInput");
  const saleRadio = document.getElementById("sale");
  const donationRadio = document.getElementById("donation");

  let lastPriceValue = '';

  function formatCurrency(value) {
    return "₩ " + Number(value).toLocaleString();
  }

  function togglePriceField() {
    if (donationRadio.checked) {
      lastPriceValue = priceInput.value;
      priceDisplay.disabled = true;
      priceDisplay.value = "₩ 0";
      priceInput.value = 0;
      priceDisplay.classList.add("bg-light");
    } else {
      priceDisplay.disabled = false;
      priceDisplay.classList.remove("bg-light");
      priceInput.value = lastPriceValue || '';
      priceDisplay.value = lastPriceValue ? formatCurrency(lastPriceValue) : '';
    }
  }

  priceDisplay.addEventListener("input", (e) => {
    let raw = e.target.value.replace(/[^0-9]/g, '');
    if (raw.length > 9) raw = raw.slice(0, 9);
    priceDisplay.value = raw ? formatCurrency(raw) : '';
    priceInput.value = raw;
    lastPriceValue = raw;
  });

  togglePriceField();
  saleRadio.addEventListener("change", togglePriceField);
  donationRadio.addEventListener("change", togglePriceField);

  // ---------------- 희망 거래 방식 (위치/택배비) ----------------
  const isDirect = document.getElementById("isDirect");
  const isDelivery = document.getElementById("isDelivery");
  const isNonface = document.getElementById("isNonface");
  const locationGroup = document.getElementById("locationGroup");
  const shippingFeeGroup = document.getElementById("shippingFeeGroup");

  function toggleFields() {
    locationGroup.style.display = (isDirect.checked || isDelivery.checked) ? "block" : "none";
    shippingFeeGroup.style.display = isNonface.checked ? "block" : "none";
  }

  isDirect.addEventListener("change", toggleFields);
  isDelivery.addEventListener("change", toggleFields);
  isNonface.addEventListener("change", toggleFields);
  toggleFields();

  // ---------------- 지도 모달 처리 ----------------
  let map, geocoder, centerMarker;
  const locationModal = document.getElementById("locationModal");

  locationModal.addEventListener("shown.bs.modal", () => {
    if (!map) {
      const container = document.getElementById("map");
      const options = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 3
      };

      map = new kakao.maps.Map(container, options);
      geocoder = new kakao.maps.services.Geocoder();

      centerMarker = new kakao.maps.Marker({
        map: map,
        position: map.getCenter()
      });

      kakao.maps.event.addListener(map, 'center_changed', () => {
        const center = map.getCenter();
        centerMarker.setPosition(center);

        // ⭐ 중심 좌표가 바뀔 때마다 주소 조회
        geocoder.coord2Address(center.getLng(), center.getLat(), (result, status) => {
          if (status === kakao.maps.services.Status.OK) {
            const address = result[0].address.address_name;
            document.getElementById("overlay-address").textContent = "📍 " + address;
          }
        });
      });
    }
  });

  // 선택 완료 시 지도 위에도 주소 보여주기
  document.getElementById("confirmLocation").addEventListener("click", () => {
    const center = map.getCenter();
    const lat = center.getLat();
    const lng = center.getLng();
    const userDetail = document.getElementById("customLocationDetail").value; // ⬅ 사용자 입력

    geocoder.coord2Address(lng, lat, (result, status) => {
      if (status === kakao.maps.services.Status.OK) {
        const baseAddress = result[0].address.address_name;

        const finalAddress = userDetail ? `${baseAddress} ${userDetail}` : baseAddress;

        document.getElementById("locationText").value = finalAddress;
        document.getElementById("latitude").value = lat;
        document.getElementById("longitude").value = lng;
        document.getElementById("selected-address").textContent = "선택된 위치: " + finalAddress;
        document.getElementById("overlay-address").textContent = "📍 " + finalAddress;

        // 💡 상품 등록 폼 미리보기 지도에도 출력
        renderPreviewMap(lat, lng);
      }
    });
  });
  function renderPreviewMap(lat, lng) {
    const previewContainer = document.getElementById("mapPreview");
    previewContainer.style.display = "block";
    const previewMap = new kakao.maps.Map(previewContainer, {
      center: new kakao.maps.LatLng(lat, lng),
      level: 4
    });

    new kakao.maps.Marker({
      map: previewMap,
      position: new kakao.maps.LatLng(lat, lng)
    });
  }

});
