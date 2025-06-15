function filmLoader(page) {
    let genreValue = document.getElementById('movieGenre').value;
    let nameValue = document.getElementById('movieName').value;
    let sortByValue = document.getElementById('sortBy').value;

    console.log(page);
    console.log(genreValue);
    console.log(nameValue);
    console.log(sortByValue);

    if (page == null) {
        page = 1;
    }

    if (genreValue == 0) {
        genreValue = null;
    }

    if (sortByValue === 'none') {
        sortByValue = null;
    }

    $.ajax({
        url: "/movies",
        type: "get",
        data: {
            page: page,
            movieGenre: genreValue,
            movieName: nameValue,
            sortBy: sortByValue,
            isFirst : false
        },
        success: function (data) {
            let container = document.querySelector('section.movie-section');
            container.innerHTML = data;
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}
