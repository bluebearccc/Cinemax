let currentSlide = 0;
const slides = document.querySelectorAll('.slide');
const dots = document.querySelectorAll('.nav-dot');
let slideInterval;

function showSlide(index) {
    // Remove active class from all slides and dots
    slides.forEach(slide => slide.classList.remove('active'));
    dots.forEach(dot => dot.classList.remove('active'));

    // Add active class to current slide and dot
    slides[index].classList.add('active');
    dots[index].classList.add('active');

    currentSlide = index;
}

function nextSlide() {
    currentSlide = (currentSlide + 1) % slides.length;
    showSlide(currentSlide);
}

function goToSlide(index) {
    clearInterval(slideInterval);
    showSlide(index);
    startSlideshow();
}

function startSlideshow() {
    slideInterval = setInterval(nextSlide, 5000); // Change slide every 5 seconds
}

// Start automatic slideshow
startSlideshow();

// Smooth animations on load
window.addEventListener('load', () => {
    const slideContent = document.querySelector('.slide-content');
    slideContent.style.animation = 'slideInLeft 1s ease-out';
});

// Add slide-in animation
const style = document.createElement('style');
style.textContent = `
            @keyframes slideInLeft {
                from {
                    opacity: 0;
                    transform: translateX(-100px) translateY(-50%);
                }
                to {
                    opacity: 1;
                    transform: translateX(0) translateY(-50%);
                }
            }
        `;
document.head.appendChild(style);

document.querySelectorAll('.carouselMovie-container').forEach(container => {
    const carouselMovie = container.querySelector('.carouselMovie');
    const cardsMovie = container.querySelectorAll('.movie-card');
    const totalcardsMovie = cardsMovie.length;

    let currentSlideMovie = 0;

    function getcardsMovieToShow() {
        const width = window.innerWidth;
        if (width < 600) return 1;
        if (width < 900) return 2;
        if (width < 1200) return 3;
        return 4;
    }

    let cardsMovieToShow = getcardsMovieToShow();
    let maxSlidesMovie = Math.max(0, totalcardsMovie - cardsMovieToShow);

    function updatecarouselMovie() {
        const cardWidth = carouselMovie.clientWidth / cardsMovieToShow;
        const offset = -currentSlideMovie * cardWidth;
        carouselMovie.style.transform = `translateX(${offset}px)`;
    }

    function nextSlideMovie() {
        currentSlideMovie = (currentSlideMovie + 1) > maxSlidesMovie ? 0 : currentSlideMovie + 1;
        updatecarouselMovie();
    }

    function prevSlideMovie() {
        currentSlideMovie = (currentSlideMovie - 1) < 0 ? maxSlidesMovie : currentSlideMovie - 1;
        updatecarouselMovie();
    }

    function init() {
        cardsMovieToShow = getcardsMovieToShow();
        maxSlidesMovie = Math.max(0, totalcardsMovie - cardsMovieToShow);
        currentSlideMovie = Math.min(currentSlideMovie, maxSlidesMovie);
        updatecarouselMovie();
    }

    let autoPlayInterval = setInterval(() => nextSlideMovie(), 2000);

    container.addEventListener('mouseenter', () => clearInterval(autoPlayInterval));
    container.addEventListener('mouseleave', () => {
        autoPlayInterval = setInterval(() => nextSlideMovie(), 2000);
    });

    // Gán sự kiện cho nút
    const prevBtn = container.querySelector('.prev-btn');
    const nextBtn = container.querySelector('.next-btn');

    if (prevBtn) prevBtn.addEventListener('click', prevSlideMovie);
    if (nextBtn) nextBtn.addEventListener('click', nextSlideMovie);

    // Responsive
    window.addEventListener('resize', init);
    init();
});


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

function attachLoadMoreEvent() {
    document.getElementById('load-feedback')?.addEventListener('click', function () {
        let currentNumOfFeedback = parseInt(document.getElementById("currentNumberOfFeedback").value);

        $.ajax({
            url: "/loadFeedback", type: "get", data: {
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
            const selectedIndex = parseInt(this.dataset.index);
            const theaterId = parseInt(document.querySelector('div.cinema-item.active input').value);

            $.ajax({
                url: "/loadBookMovie",
                type: "get",
                data: {
                    selectedIndex: selectedIndex,
                    theaterId: theaterId
                },
                success: function (data) {
                    console.log(data);
                    document.querySelector('div.book-movie-area').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachCinemaItemEvents();
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
            const hee = document.querySelector('div.book-movie-area');

            $.ajax({
                url: "/loadBookMovie",
                type: "get",
                data: {
                    selectedIndex: selectedIndex,
                    theaterId: theaterId
                },
                success: function (data) {
                    console.log(data);
                    document.querySelector('.book-movie-area').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachCinemaItemEvents();
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


