function filmLoader(page) {
    let genreValue = parseInt(document.querySelector('.filter-btn.active').dataset.genreId);
    let nameValue = document.getElementById('movieName').value;
    let sortByValue = document.querySelector('.sort-btn.active')?.dataset.sort;
    let theaterId = parseInt(document.querySelector('.cinema-option.active .cinema-id').value);

    console.log(page);
    console.log(genreValue);
    console.log(nameValue);
    console.log(sortByValue);
    console.log(theaterId);

    if (theaterId === 0) {
        theaterId = null;
    }

    if (page == null) {
        page = 1;
    }

    if (genreValue === 0) {
        genreValue = null;
    }

    if (sortByValue === 'none' || sortByValue === 'view') {
        sortByValue = null;
    }

    $.ajax({
        url: "/customer/movies",
        type: "get",
        data: {
            page: page,
            movieGenre: genreValue,
            movieName: nameValue,
            sortBy: sortByValue,
            selectedTheater: theaterId,
            isFirst: false
        },
        success: function (data) {
            let container = document.getElementById('list-movie-wrapper');
            console.log(data);
            container.innerHTML = data;
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}

// Cinema dropdown functionality
function toggleCinemaDropdown() {
    const dropdown = document.getElementById('cinemaDropdown');
    const button = document.querySelector('.cinema-button');

    dropdown.classList.toggle('show');
    button.classList.toggle('open');
}

function attachGenreItemEvents() {
    let genreItem = document.querySelectorAll('.filter-btn');

    genreItem.forEach(item => {
        item.addEventListener('click', function () {
            genreItem.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            filmLoader(null);
        });
    });
}

function attachCinemaEvents() {
    let cinemaItem = document.querySelectorAll('.cinema-option');
    let buttonCinema = document.querySelector('.cinema-button');
    let cinemaxText = buttonCinema.querySelector('.cinema-option-text');

    cinemaItem.forEach(item => {
        item.addEventListener('click', function () {
            cinemaItem.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            cinemaxText.textContent = item.querySelector('.cinema-option-text').textContent;
            filmLoader(null);
        });
    });
}

function attachSortButton() {
    // Event listeners
    const sortButton = document.getElementById('sortButton');
    const sortOptions = document.getElementById('sortOptions');
    const overlay = document.getElementById('overlay');
    const sortOptionItem = document.querySelectorAll('.sort-btn');
    const sortDisplayButton = sortButton.querySelector('.sort-option-selected');

    sortOptionItem.forEach(item => {
        item.addEventListener('click', function () {
            sortOptionItem.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            sortDisplayButton.textContent = item.querySelector('.sort-option-text').textContent;

            filmLoader(null);
        });
    });


    sortButton.addEventListener('click', () => {
        sortOptions.classList.toggle('show');
        overlay.classList.toggle('show');
    });

    overlay.addEventListener('click', () => {
        sortOptions.classList.remove('show');
        overlay.classList.remove('show');
    });
}

document.addEventListener('DOMContentLoaded', attachSortButton);
document.addEventListener('DOMContentLoaded', attachGenreItemEvents);
document.addEventListener('DOMContentLoaded', attachCinemaEvents);






