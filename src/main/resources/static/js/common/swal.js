/** 전역 SweetAlert 호춝함수 */
window.showAlert = function(icon, title, text, timer = 1500) {
    Swal.fire({
        icon: icon,
        title: title,
        text: text,
        timer: timer,
        showConfirmButton: false
    });
};


