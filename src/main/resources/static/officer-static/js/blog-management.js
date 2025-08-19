let categoryPanelCollapsed = false;
let isAdd = true;

function toggleCategoryPanel() {
    const panel = document.getElementById('categoryPanel');
    const title = document.getElementById('categoryTitle');
    const content = document.getElementById('categoryContent');
    const button = document.querySelector('.toggle-btn');

    categoryPanelCollapsed = !categoryPanelCollapsed;

    if (categoryPanelCollapsed) {
        panel.classList.add('collapsed');
        title.style.display = 'none';
        content.style.display = 'none';
        button.textContent = 'open';
    } else {
        panel.classList.remove('collapsed');
        title.style.display = 'block';
        content.style.display = 'block';
        button.textContent = 'close';
    }
}

function openAddCategoryModal() {
    isAdd = true;
    document.getElementById('activity').value = 'add';
    document.getElementById('categoryModalTitle').textContent = 'Add Category';
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryModal').style.display = 'block';
}

function editCategory(e) {
    const category = e.closest('.category-item');
    if (!category) return;

    isAdd = false;
    document.getElementById('categoryID').value = category.querySelector('.category-id').value;
    document.getElementById('activity').value = 'edit';
    document.getElementById('categoryModalTitle').textContent = 'Edit Category';
    document.getElementById('categoryName').value = category.querySelector('.category-name').textContent;
    document.getElementById('categoryModal').style.display = 'block';
}

function closeCategoryModal() {
    const errorElement = document.getElementById("categoryError");
    errorElement.textContent = "";
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryModal').style.display = 'none';
}

function deleteCategory(id) {
    if (confirm('Are you sure you want to delete this category?')) {
        $.ajax({
            url: "/officer/blog-management/delete-category",
            type: "post",
            data: {
                categoryId: parseInt(id),
            },
            success: function (data) {
                let container = document.getElementById('category-list-mapper');
                container.innerHTML = data;
                showSuccessAlert();
            },
            error: function (e) {
                alert('Lỗi khi tải trang: ' + e.statusText);
            }
        });
    }
}

window.onclick = function (event) {
    const categoryModal = document.getElementById('categoryModal');

    if (event.target == categoryModal) {
        closeCategoryModal();
    }
}

function filterBlog(page) {
    let genreValue = parseInt(document.getElementById('sortSelect').value);
    let nameValue = document.getElementById('searchInput').value;

    if (page == null) {
        page = 1;
    }

    if (genreValue === 0) {
        genreValue = null;
    }

    $.ajax({
        url: "/officer/blog-management/load-blog",
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

function validateCategory() {
    const name = document.getElementById('categoryName');
    const trimmed = name.value.trim();
    const errorElement = document.getElementById("categoryError");
    if (trimmed.length <= 1) {
        errorElement.textContent = "Name must be more than 1 character and not empty.";
        errorElement.style.display = "block";
    } else {
        errorElement.textContent = "";
        errorElement.style.display = "none";
    }
}

function interactCategory(event) {
    event.preventDefault();
    const categoryName = document.getElementById('categoryName').value.trim();
    closeCategoryModal();

    if (isAdd) {
        sendAddCategory(categoryName);
    } else {
        sendEditCategory(categoryName, parseInt(document.getElementById('categoryID').value));
    }

}

function sendAddCategory(categoryName) {
    $.ajax({
        url: "/officer/blog-management/add-category",
        type: "post",
        data: {
            categoryName: categoryName,
        },
        success: function (data) {
            let container = document.getElementById('category-list-mapper');
            container.innerHTML = data;
            showSuccessAlert();
            showErrorAlert();
            reloadCategorySelection();
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}

function sendEditCategory(categoryName, categoryId) {
    $.ajax({
        url: "/officer/blog-management/edit-category",
        type: "post",
        data: {
            categoryName: categoryName,
            categoryId: categoryId
        },
        success: function (data) {
            let container = document.getElementById('category-list-mapper');
            container.innerHTML = data;
            showSuccessAlert();
            showErrorAlert();
            reloadCategorySelection();
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}

function reloadCategorySelection() {
    $.ajax({
        url: "/officer/blog-management/reload-category-select",
        type: "get",
        success: function (data) {
            let container = document.getElementById('sortSelect');
            container.innerHTML = data;
        },
        error: function (e) {
            alert('Lỗi khi tải trang: ' + e.statusText);
        }
    });
}

function showSuccessAlert() {
    const alertBox = document.getElementById("customAlert");
    if (alertBox != null) {
        alertBox.style.display = "block";

        // Ẩn alert sau animation (3s)
        setTimeout(() => {
            alertBox.style.display = "none";
        }, 3000);
    }

}

function showErrorAlert() {
    const alertBox = document.getElementById("customError");
    if (alertBox != null) {
        alertBox.style.display = "block";

        // Ẩn alert sau animation (3s)
        setTimeout(() => {
            alertBox.style.display = "none";
        }, 3000);
    }

}

window.onload = function () {
    console.log("CUSTOM ALERT EXISTS?", document.getElementById("customAlert"));
    showSuccessAlert();
    showErrorAlert();
};

