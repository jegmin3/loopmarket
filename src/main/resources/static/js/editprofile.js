$(function() {
  var $profileImageInput = $('#profileImage');
  var $phoneInput = $('#phoneNumber');
  var $form = $profileImageInput.closest('form');  // form 요소 한 번만 찾기

  var validImage = true; // 기본적으로 이미지 유효하다고 가정

  // 프로필 이미지 크기 체크
  $profileImageInput.on('change', function(event) {
    var file = event.target.files[0];
    if (!file) {
      validImage = true;  // 파일 없으면 문제 없다고 판단
      return;
    }

    var reader = new FileReader();

    reader.onload = function(e) {
      var img = new Image();
      img.onload = function() {
        if (img.width !== 100 || img.height !== 100) {
          alert('프로필 이미지는 반드시 100x100 크기여야 합니다.');
          $profileImageInput.val(''); // 선택 초기화
          validImage = false;         // 이미지 유효하지 않음 표시
        } else {
          validImage = true;          // 이미지 유효함
        }
      };
      img.src = e.target.result;
    };

    reader.readAsDataURL(file);
  });

  // 폼 제출 전에 전화번호 유효성 검사 및 이미지 크기 검사
  $form.on('submit', function(event) {
    // 전화번호 검사
    var phone = $phoneInput.val().trim();
    if (phone !== '') {
      var phoneRegex = /^\d{2,3}-\d{3,4}-\d{4}$/;
      if (!phoneRegex.test(phone)) {
        alert('전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678');
        $phoneInput.focus();
        event.preventDefault();
        return false;
      }
    }

    // 이미지 크기 검사
    if ($profileImageInput.val() !== '' && !validImage) {
      alert('프로필 이미지가 100x100 크기가 아닙니다. 수정해 주세요.');
      event.preventDefault();
      return false;
    }
  });
});