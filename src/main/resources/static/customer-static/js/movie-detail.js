function generateDates(selectedIndex = 0) {
    const dateSelector = document.getElementById('date-selector');
    dateSelector.innerHTML = '';
    const today = new Date();
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    for (let i = 0; i < 7; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);

        const dayLabel = i === 0 ? 'Today' : dayNames[date.getDay()];

        const dateItem = document.createElement('div');
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
    const modal = document.getElementById('trailerModal');
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
    const modal = document.getElementById('trailerModal');
    const iframe = document.getElementById("trailerFrame");
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
    const modal = document.getElementById('trailerModal');
    if (event.target === modal) {
        closeTrailer();
    }
}

// Add parallax effect to hero section
window.addEventListener('scroll', function () {
    const scrolled = window.pageYOffset;
    const heroSection = document.querySelector('.hero-section');
    heroSection.style.transform = `translateY(${scrolled * 0.5}px)`;
});

// Add hover effect to poster
const poster = document.querySelector('.movie-poster');
poster.addEventListener('mousemove', function (e) {
    const rect = this.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    const rotateX = (y - centerY) / 10;
    const rotateY = (centerX - x) / 10;

    this.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale(1.05)`;
});

poster.addEventListener('mouseleave', function () {
    this.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale(1)';
});

document.querySelectorAll('.carouselCast-container').forEach(container => {
    const carouselCast = container.querySelector('.carouselCast');
    const cardsCast = container.querySelectorAll('.cast-card');
    const totalcardsCast = cardsCast.length;

    let currentSlideCast = 0;

    function getcardsCastToShow(numberOfActor) {
        // const width = window.innerWidth;
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
        const cardWidth = carouselCast.clientWidth / cardsCastToShow;
        const offset = -currentSlideCast * cardWidth;
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
    const prevBtn = container.querySelector('.prev-btn');
    const nextBtn = container.querySelector('.next-btn');

    if (prevBtn) prevBtn.addEventListener('click', prevSlideCast);
    if (nextBtn) nextBtn.addEventListener('click', nextSlideCast);

    // Responsive
    window.addEventListener('resize', init);
    init();
});

function attachLoadMoreEvent() {
    document.getElementById('load-feedback')?.addEventListener('click', function () {
        let currentNumOfFeedback = parseInt(document.getElementById("currentNumberOfFeedback").value);

        $.ajax({
            url: "/customer/movie-detail/loadfeedback", type: "get", data: {
                currentNumberOfFeedback: currentNumOfFeedback
            }, success: function (data) {
                let container = document.querySelector('div.feedback-area');
                container.innerHTML = data;
                // Gắn lại listener sau khi innerHTML xong
                attachLoadMoreEvent();
            }, error: function (e) {
                alert('Lỗi khi tải trang: ' + e.statusText);
            }
        });
    });
}

function attachDateItemEvents() {
    const dateItems = document.querySelectorAll('.date-item');

    dateItems.forEach(item => {
        item.addEventListener('click', function () {
            dateItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const selectedIndex = parseInt(this.dataset.index);
            const theaterId = parseInt(document.querySelector('div.cinema-item.active input').value);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;
            const movieId = parseInt(document.getElementById('movieId').value);

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
    const cinemaItems = document.querySelectorAll('.cinema-item');
    cinemaItems.forEach(item => {
        item.addEventListener('click', function () {
            cinemaItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            const selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            const theaterId = parseInt(item.querySelector('input').value);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;
            const movieId = parseInt(document.getElementById('movieId').value);

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
                    console.log(data);
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
    const roomButtonItems = document.querySelectorAll('.custom-button-item');
    roomButtonItems.forEach(item => {
        item.addEventListener('click', () => {
            roomButtonItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            const selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            const theaterId = parseInt(document.querySelector('div.cinema-item.active input').value);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;
            const movieId = parseInt(document.getElementById('movieId').value);

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
                    console.log(data);
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

document.addEventListener('DOMContentLoaded', attachLoadMoreEvent);
document.addEventListener('DOMContentLoaded', attachDateItemEvents);
document.addEventListener('DOMContentLoaded', attachCinemaItemEvents);
document.addEventListener('DOMContentLoaded', attachRoomItemEvents);

