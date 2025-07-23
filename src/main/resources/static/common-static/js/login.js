document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const redirectUrl = urlParams.get('redirect');
    const redirectInput = document.getElementById('redirectValue');
    if (redirectUrl && redirectInput) {
        redirectInput.value = redirectUrl;
    }
});