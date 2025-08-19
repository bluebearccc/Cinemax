function attackLikeEvents() {
// Like functionality
    const likeSection = document.getElementById('likeSection');
    const likeCount = document.getElementById('likeCount');
    const heartIcon = likeSection.querySelector('.heart-icon');
    let blogId = document.getElementById('blogId').value;
    let currentLikes = parseInt(likeCount.textContent) || 0;

    likeSection.addEventListener('click', function () {

        const customerId = document.getElementById('customerId');
        if (customerId == null) {
            window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
            return;
        }

        // Toggle class 'liked'
        const isNowLiked = likeSection.classList.toggle('liked');

        if (isNowLiked) {
            heartIcon.textContent = '♥';
            currentLikes++;
        } else {
            heartIcon.textContent = '♡';
            currentLikes--;
        }

        likeCount.textContent = currentLikes;

        $.ajax({
            url: "/customer/blog-detail/like",
            type: "post",
            data: {
                blogId: blogId
            },
        });

    });

}

document.addEventListener('DOMContentLoaded', attackLikeEvents);




