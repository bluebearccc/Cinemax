function filmLoader(page) {
    let genreValue = document.getElementById('movieGenre').value;
    let theaterId = document.getElementById('theater').value;
    let nameValue = document.getElementById('movieName').value;
    let sortByValue = document.getElementById('sortBy').value;

    console.log('hehehehe');
    console.log(page);
    console.log(genreValue);
    console.log(nameValue);
    console.log(sortByValue);
    console.log(theaterId);

    if (theaterId === 'none') {
        theaterId = null;
    }

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
        url: "/customer/movies",
        type: "get",
        data: {
            page: page,
            movieGenre: genreValue,
            movieName: nameValue,
            sortBy: sortByValue,
            selectedTheater : theaterId,
            isFirst : false
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


