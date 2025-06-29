function generateDates(selectedIndex = 0) {
    let dateSelector = document.getElementById('date-selector');
    dateSelector.innerHTML = '';
    let today = new Date();
    let dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    for (let i = 0; i < 14; i++) {
        let date = new Date(today);
        date.setDate(today.getDate() + i);

        let dayLabel = i === 0 ? 'Today' : dayNames[date.getDay()];

        let dateItem = document.createElement('div');
        dateItem.className = 'date-item';
        dateItem.dataset.index = i; // 👈 dùng index (0–6)

        if (i === selectedIndex) {
            dateItem.classList.add('active');
        }

        dateItem.innerHTML = `
            <div class="date-number">${date.getDate()}</div>
            <div class="date-day">${dayLabel}</div>
        `;

        dateSelector.appendChild(dateItem);
    }
}

generateDates();

function openTrailer() {
    let modal = document.getElementById('trailerModal');
    modal.style.display = 'flex';
    document.body.style.overflow = 'hidden';

    // Add fade in animation
    modal.style.opacity = '0';
    setTimeout(() => {
        modal.style.opacity = '1';
        modal.style.transition = 'opacity 0.3s ease';
    }, 10);
}

function closeTrailer() {
    let modal = document.getElementById('trailerModal');
    let iframe = document.getElementById("trailerFrame");
    var videoSrc = iframe.src;
    iframe.src = videoSrc;
    modal.style.opacity = '0';
    setTimeout(() => {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }, 300);
}

// Close modal when clicking outside
window.onclick = function (event) {
    let modal = document.getElementById('trailerModal');
    if (event.target === modal) {
        closeTrailer();
    }
}

// Add parallax effect to hero section
window.addEventListener('scroll', function () {
    let scrolled = window.pageYOffset;
    let heroSection = document.querySelector('.hero-section');
    heroSection.style.transform = `translateY(${scrolled * 0.5}px)`;
});

// Add hover effect to poster
let poster = document.querySelector('.movie-poster');
poster.addEventListener('mousemove', function (e) {
    let rect = this.getBoundingClientRect();
    let x = e.clientX - rect.left;
    let y = e.clientY - rect.top;
    let centerX = rect.width / 2;
    let centerY = rect.height / 2;
    let rotateX = (y - centerY) / 10;
    let rotateY = (centerX - x) / 10;

    this.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.05)`;
});

poster.addEventListener('mouseleave', function () {
    this.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1)';
});

document.querySelectorAll('.carouselCast-container').forEach(container => {
    let carouselCast = container.querySelector('.carouselCast');
    let cardsCast = container.querySelectorAll('.cast-card');
    let totalcardsCast = cardsCast.length;

    let currentSlideCast = 0;

    function getcardsCastToShow(numberOfActor) {
        // let width = window.innerWidth;
        // if (width < 600) return 2;
        // if (width < 900) return 3;
        // if (width < 1200) return 4;
        // return 5;
        //
        // if (numberOfActor == 7) return 3;
        // if (numberOfActor == 8) return 4;
        // if (numberOfActor == 9) return 5;
        return 5;
    }

    let cardsCastToShow = getcardsCastToShow();
    let maxSlidesCast = Math.max(0, totalcardsCast - cardsCastToShow);

    function updatecarouselCast() {
        let cardWidth = carouselCast.clientWidth / cardsCastToShow;
        let offset = -currentSlideCast * cardWidth;
        carouselCast.style.transform = `translateX(${offset}px)`;
    }

    function nextSlideCast() {
        currentSlideCast = (currentSlideCast + 1) > maxSlidesCast ? 0 : currentSlideCast + 1;
        updatecarouselCast();
    }

    function prevSlideCast() {
        currentSlideCast = (currentSlideCast - 1) < 0 ? maxSlidesCast : currentSlideCast - 1;
        updatecarouselCast();
    }

    function init() {
        cardsCastToShow = getcardsCastToShow();
        maxSlidesCast = Math.max(0, totalcardsCast - cardsCastToShow);
        currentSlideCast = Math.min(currentSlideCast, maxSlidesCast);
        updatecarouselCast();
    }

    let autoPlayInterval = setInterval(() => nextSlideCast(), 4000);

    container.addEventListener('mouseenter', () => clearInterval(autoPlayInterval));
    container.addEventListener('mouseleave', () => {
        autoPlayInterval = setInterval(() => nextSlideCast(), 4000);
    });

    // Gán sự kiện cho nút
    let prevBtn = container.querySelector('.prev-btn');
    let nextBtn = container.querySelector('.next-btn');

    if (prevBtn) prevBtn.addEventListener('click', prevSlideCast);
    if (nextBtn) nextBtn.addEventListener('click', nextSlideCast);

    // Responsive
    window.addEventListener('resize', init);
    init();
});

function attachDateItemEvents() {
    let dateItems = document.querySelectorAll('.date-item');

    dateItems.forEach(item => {
        item.addEventListener('click', function () {
            dateItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            let selectedIndex = parseInt(this.dataset.index);
            let theaterId = parseInt(document.querySelector('div.cinema-item.active input').value);
            let roomType = document.querySelector('.custom-button-item.active span').innerText;
            let movieId = parseInt(document.getElementById('movieId').value);

            $.ajax({
                url: "/customer/movie-detail/loadSchedule",
                type: "get",
                data: {
                    selectedIndex: selectedIndex,
                    theaterId: theaterId,
                    roomType: roomType,
                    movieId: movieId
                },
                success: function (data) {
                    document.getElementById('book-detail-showtimes').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachCinemaItemEvents();
                    attachRoomItemEvents();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

function attachCinemaItemEvents() {
    let cinemaItems = document.querySelectorAll('.cinema-item');
    cinemaItems.forEach(item => {
        item.addEventListener('click', function () {
            cinemaItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            let selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            let theaterId = parseInt(item.querySelector('input').value);
            let roomType = document.querySelector('.custom-button-item.active span').innerText;
            let movieId = parseInt(document.getElementById('movieId').value);

            $.ajax({
                url: "/customer/movie-detail/loadSchedule",
                type: "get",
                data: {
                    selectedIndex: selectedIndex,
                    theaterId: theaterId,
                    roomType: roomType,
                    movieId: movieId
                },
                success: function (data) {
                    document.getElementById('book-detail-showtimes').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachCinemaItemEvents();
                    attachRoomItemEvents();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

function attachRoomItemEvents() {
    let roomButtonItems = document.querySelectorAll('.custom-button-item');
    roomButtonItems.forEach(item => {
        item.addEventListener('click', () => {
            roomButtonItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            let selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            let theaterId = parseInt(document.querySelector('div.cinema-item.active input').value);
            let roomType = document.querySelector('.custom-button-item.active span').innerText;
            let movieId = parseInt(document.getElementById('movieId').value);

            $.ajax({
                url: "/customer/movie-detail/loadSchedule",
                type: "get",
                data: {
                    selectedIndex: selectedIndex,
                    theaterId: theaterId,
                    roomType: roomType,
                    movieId: movieId
                },
                success: function (data) {
                    document.getElementById('book-detail-showtimes').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachCinemaItemEvents();
                    attachRoomItemEvents();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

function attachLoadMoreEvent() {
    document.getElementById('load-feedback')?.addEventListener('click', function () {
        let currentPage = parseInt(document.getElementById("currentPage").value);
        let movieId = parseInt(document.getElementById("movieId").value);

        $.ajax({
            url: "/customer/movie-detail/loadFeedback", type: "get", data: {
                currentPage: currentPage,
                movieId: movieId
            }, success: function (data) {
                let container = document.querySelector('div.comments-section');
                container.innerHTML = data;
                // Gắn lại listener sau khi innerHTML xong
                attachLoadMoreEvent();
                attachShowMoreCommentEvent();
                initializeCommentSection();
            }, error: function (e) {
                alert('Lỗi khi tải trang: ' + e.statusText);
            }
        });
    });
}

function attachShowMoreCommentEvent() {
    let showMore = document.querySelectorAll('.load-more-replies-movie-detail');
    showMore.forEach(item => {
        item.addEventListener('click', (e) => {
            let div = e.target.closest('.replies-movie-detail');
            let currentNumOfComment = parseInt(div.querySelector('.currentNumOfComment').value);
            let feedbackId = parseInt(div.querySelector('.feedbackId').value);
            $.ajax({
                url: "/customer/movie-detail/loadComment",
                type: "get",
                data: {
                    currentNumOfComment: currentNumOfComment,
                    feedbackId: feedbackId
                },
                success: function (data) {
                    div.closest('.replies-ajax').innerHTML = data;
                    attachLoadMoreEvent();
                    attachShowMoreCommentEvent();
                    initializeCommentSection();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

document.addEventListener('DOMContentLoaded', attachLoadMoreEvent);
document.addEventListener('DOMContentLoaded', attachDateItemEvents);
document.addEventListener('DOMContentLoaded', attachCinemaItemEvents);
document.addEventListener('DOMContentLoaded', attachRoomItemEvents);
document.addEventListener('DOMContentLoaded', attachShowMoreCommentEvent);

function toggleReplyForm(button, authorId) {
    let parentComment = button.closest('.comment-content-movie-detail');
    let existingForm = parentComment.querySelector('.reply-form-container-movie-detail');
    if (existingForm) {
        existingForm.remove();
        return;
    }

    let replyFormContainer = document.createElement('div');
    replyFormContainer.className = 'reply-form-container-movie-detail';
    replyFormContainer.innerHTML = `
        <form class="reply-form-movie-detail" data-author-id="${authorId}">
            <div class="comment-form-movie-detail" style="margin-top: 10px;">
                <div class="comment-avatar"><i class="fas fa-user"></i></div>
                <div class="comment-input-wrapper-movie-detail">
                    <textarea name="text" class="comment-input-movie-detail" placeholder="Write your thought..." rows="1" required></textarea>
                    <div class="form-actions-movie-detail" style="display: flex;">
                        <button type="button" class="btn-cancel-movie-detail">Cancel</button>
                        <button type="submit" class="btn-submit-movie-detail">Submit</button>
                    </div>
                </div>
            </div>
        </form>
    `;
    parentComment.appendChild(replyFormContainer);
    replyFormContainer.querySelector('.comment-input-movie-detail').focus();
}

function updateStarsOnClick(star) {
    let ratingContainer = star.parentElement;
    let ratingValue = parseInt(star.dataset.value);
    let stars = ratingContainer.querySelectorAll('.star-movie-detail');
    let hiddenInput = ratingContainer.querySelector('.rating-value-movie-detail');

    if (hiddenInput) {
        hiddenInput.value = ratingValue;
    }

    stars.forEach((s, index) => {
        s.classList.toggle('fas', index < ratingValue);
        s.classList.toggle('far', index >= ratingValue);
    });
}

function previewStarsOnHover(star) {
    let ratingContainer = star.parentElement;
    let hoverValue = parseInt(star.dataset.value);
    let stars = ratingContainer.querySelectorAll('.star-movie-detail');

    stars.forEach((s, index) => {
        s.classList.toggle('fas', index < hoverValue);
        s.classList.toggle('far', index >= hoverValue);
    });
}

function resetStarsOnMouseOut(ratingContainer) {
    let stars = ratingContainer.querySelectorAll('.star-movie-detail');
    let hiddenInput = ratingContainer.querySelector('.rating-value-movie-detail');
    let selectedValue = parseInt(hiddenInput?.value) || 0;

    stars.forEach((s, index) => {
        s.classList.toggle('fas', index < selectedValue);
        s.classList.toggle('far', index >= selectedValue);
    });
}

function resetStarSelectionInForm(form) {
    form.querySelectorAll('.star-movie-detail').forEach(s => {
        s.classList.remove('fas');
        s.classList.add('far');
    });
    let hiddenInput = form.querySelector('.rating-value-movie-detail');
    if (hiddenInput) {
        hiddenInput.value = '';
    }
}

function handleCommentSectionClick(e) {
    let target = e.target;
    if (target.classList.contains('reply-btn-movie-detail')) {
        e.preventDefault();
        let authorId = target.dataset.authorId;
        toggleReplyForm(target, authorId);
    }
    if (target.classList.contains('btn-cancel-movie-detail')) {
        target.closest('.reply-form-container-movie-detail').remove();
    }
    if (target.classList.contains('star-movie-detail')) {
        updateStarsOnClick(target);
    }
}

function handleStarHover(e) {
    if (e.target.classList.contains('star-movie-detail')) {
        previewStarsOnHover(e.target);
    }
}

function handleStarMouseOut(e) {
    if (e.target.classList.contains('star-rating-input-movie-detail')) {
        resetStarsOnMouseOut(e.target);
    }
}

function handleMainCommentSubmit(form) {
    let content = form.querySelector('.comment-input-movie-detail').value;
    let rating = form.querySelector('.rating-value-movie-detail')?.value;
    let movieId = document.querySelector('#movieId').value;
    let customerId = document.getElementById("customerId").value;
    let currentPage = document.getElementById("currentPage").value;
    let totalPage = document.getElementById("totalPage").value;

    if (rating.trim() === '') {
        rating = null;
    }

    $.ajax({
        url: "/customer/movie-detail/addFeedback", type: "post", data: {
            customerId: customerId,
            movieId: movieId,
            content: content,
            rate: rating,
            currentPage: currentPage,
            totalPage: totalPage
        }, success: function (data) {
            let container = document.querySelector('div.comments-section');
            container.innerHTML = data;
            attachLoadMoreEvent();
            attachShowMoreCommentEvent();
            initializeCommentSection();
        }, error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });


    form.reset();
    resetStarSelectionInForm(form);
    form.querySelector('.form-actions-movie-detail').style.display = 'none';
}

function handleReplySubmit(form, repliedId) {
    let bigDiv = form.closest('.bigger-comment-movie-detail');
    let content = form.querySelector('.comment-input-movie-detail').value;
    let feedbackId = parseInt(bigDiv.querySelector('.feedbackId').value);
    let authorId= document.getElementById("customerId").value
    let currentNumOfComment = parseInt(bigDiv.querySelector('.currentNumOfComment').value);
    repliedId = parseInt(repliedId);

    $.ajax({
        url: "/customer/movie-detail/addComment", type: "post", data: {
            feedbackId: feedbackId,
            authorId: authorId,
            repliedId: repliedId,
            content: content,
            currentNumOfComment: currentNumOfComment
        }, success: function (data) {
            let container = bigDiv.querySelector('.replies-ajax');
            container.innerHTML = data;
            attachLoadMoreEvent();
            attachShowMoreCommentEvent();
            initializeCommentSection();
        }, error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });

    form.closest('.reply-form-container-movie-detail').remove();
}

function handleSubmitDelegator(e) {
    e.preventDefault();
    let form = e.target;

    if (form.id === 'mainCommentFormMovieDetail') {
        handleMainCommentSubmit(form);
    } else if (form.classList.contains('reply-form-movie-detail')) {
        handleReplySubmit(form, form.dataset.authorId);
    }
}

function initializeCommentSection() {
    let commentSection = document.querySelector('.comment-section-movie-detail');
    if (!commentSection) return;

    commentSection.addEventListener('click', handleCommentSectionClick);
    commentSection.addEventListener('mouseover', handleStarHover);
    commentSection.addEventListener('mouseout', handleStarMouseOut);
    commentSection.addEventListener('submit', handleSubmitDelegator);

    let mainCommentForm = document.getElementById('mainCommentFormMovieDetail');
    if (mainCommentForm) {
        let mainTextarea = mainCommentForm.querySelector('.comment-input-movie-detail');
        let mainFormActions = mainCommentForm.querySelector('.form-actions-movie-detail');
        let mainCancelBtn = mainCommentForm.querySelector('#main-cancel-btn');

        mainTextarea.addEventListener('focus', () => {
            mainFormActions.style.display = 'flex';
        });

        mainCancelBtn.addEventListener('click', () => {
            mainTextarea.value = '';
            mainFormActions.style.display = 'none';
        });
    }
}

document.addEventListener('DOMContentLoaded', initializeCommentSection);



