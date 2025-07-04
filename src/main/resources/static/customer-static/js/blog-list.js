// Parallax effect for hero section
window.addEventListener('scroll', () => {
    const scrolled = window.pageYOffset;
    const hero = document.querySelector('.hero');
    if (hero) {
        hero.style.transform = `translateY(${scrolled * 0.5}px)`;
    }
});

function attachCategoryEvents() {
    let categoryItems = document.querySelectorAll('.filter-btn');

    categoryItems.forEach(item => {
        item.addEventListener('click', function () {
            categoryItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            filterBlog(null);
        });
    });
}

function filterBlog(page) {
    let genreValue = parseInt(document.querySelector('.filter-btn.active').dataset.genreId);
    let nameValue = document.getElementById('searchInput').value;

    if (page == null) {
        page = 1;
    }

    if (genreValue === 0) {
        genreValue = null;
    }

    $.ajax({
        url: "/customer/load-blog",
        type: "get",
        data: {
            page: page,
            categoryId: genreValue,
            title: nameValue,
        },
        success: function (data) {
            let container = document.getElementById('list-blog-wrapper');
            container.innerHTML = data;
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}

document.addEventListener('DOMContentLoaded', attachCategoryEvents);

