function previewImage(input) {
    const preview = document.getElementById('imagePreview');

    if (input.files && input.files[0]) {
        const reader = new FileReader();

        reader.onload = function (e) {
            preview.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
            preview.classList.add('has-image');
        };

        reader.readAsDataURL(input.files[0]);
    } else {
        preview.innerHTML = '<span>No image selected</span>';
        preview.classList.remove('has-image');
    }

    setImageName();
}

function setImageName() {
    const file = document.getElementById('blogImage').files[0];
    if (file) {
        document.getElementById('imageName').value = file.name;
    }
}

function closeBlogModal() {
    document.getElementById('blogForm').reset();
    const preview = document.getElementById('imagePreview');
    preview.innerHTML = '<span>No image selected</span>';
    preview.classList.remove('has-image');
    generateBlogSections(1);
}

function createSectionHTML(i) {
    return `
        <div class="section-header">
            <h4>Section ${i + 1}</h4>
            <button type="button" class="btn btn-danger btn-sm" onclick="deleteBlogSection(this)">Delete</button>
        </div>
        <div class="form-group">
            <label for="sections[${i}].sectionTitle">Section Title:</label>
            <input type="text" class="form-control" name="sections[${i}].sectionTitle" required />
        </div>
        <div class="form-group">
            <label for="sections[${i}].sectionContent">Section Content:</label>
            <textarea class="form-control" name="sections[${i}].sectionContent" rows="4" required></textarea>
        </div>
        <input type="hidden" class="form-control" name="sections[${i}].sectionOrder" value="${i + 1}"/>
    `;
}


function reindexSections() {
    const sections = document.querySelectorAll('#blogSections .section-item');
    const sectionCountInput = document.getElementById('blogSectionCount');
    sectionCountInput.value = sections.length;

    sections.forEach((section, i) => {
        section.querySelector('h4').textContent = `Section ${i + 1}`;
        section.querySelector('label[for*="sectionTitle"]').setAttribute('for', `sections[${i}].sectionTitle`);
        section.querySelector('input[name*="sectionTitle"]').setAttribute('name', `sections[${i}].sectionTitle`);
        section.querySelector('label[for*="sectionContent"]').setAttribute('for', `sections[${i}].sectionContent`);
        section.querySelector('textarea[name*="sectionContent"]').setAttribute('name', `sections[${i}].sectionContent`);
        const orderInput = section.querySelector('input[name*="sectionOrder"]');
        orderInput.setAttribute('name', `sections[${i}].sectionOrder`);
        orderInput.value = i + 1;
    });
}

function generateBlogSections(desiredCountValue) {
    const desiredCount = parseInt(desiredCountValue);
    if (isNaN(desiredCount) || desiredCount <= 0) return;

    const container = document.getElementById('blogSections');
    const currentCount = container.children.length;

    if (desiredCount > currentCount) {
        for (let i = currentCount; i < desiredCount; i++) {
            const sectionDiv = document.createElement('div');
            sectionDiv.className = 'section-item';
            sectionDiv.innerHTML = createSectionHTML(i);
            container.appendChild(sectionDiv);
        }
    } else if (desiredCount < currentCount) {
        for (let i = currentCount; i > desiredCount; i--) {
            if (container.lastElementChild) {
                container.lastElementChild.remove();
            }
        }
    }
}


function addBlogSection() {
    const container = document.getElementById('blogSections');
    const newIndex = container.children.length;
    const sectionDiv = document.createElement('div');
    sectionDiv.className = 'section-item';
    sectionDiv.innerHTML = createSectionHTML(newIndex);
    container.appendChild(sectionDiv);

    document.getElementById('blogSectionCount').value = newIndex + 1;
}

function deleteBlogSection(button) {
    button.closest('.section-item').remove();
    reindexSections();
}

document.addEventListener('DOMContentLoaded', function () {
    const initialCount = parseInt(document.getElementById('blogSectionCount').value) || 1;
    const container = document.getElementById('blogSections');
    container.innerHTML = '';
    for(let i=0; i<initialCount; i++) {
        addBlogSection();
    }
});

function validateField(field) {
    const parent = field.parentNode;
    const existingError = parent.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }
    field.classList.remove('is-invalid');

    const value = field.value.trim();
    let isValid = true;

    if (value.length <= 1) {
        isValid = false;
        field.classList.add('is-invalid');

        const error = document.createElement('div');
        error.className = 'error-message';
        error.textContent = 'Input must be more than one character.';
        parent.appendChild(error);
    }

    return isValid;
}


function validateForm() {
    let isFormValid = true;
    const fieldsToValidate = document.querySelectorAll(
        '#blogForm input[type="text"][required], #blogForm textarea[required]'
    );

    fieldsToValidate.forEach(field => {
        if (!validateField(field)) {
            isFormValid = false;
        }
    });

    return isFormValid;
}


document.addEventListener('DOMContentLoaded', function () {
    const requiredTextareas = document.querySelectorAll('textarea[required]');
    const requiredInputs = document.querySelectorAll('input[type="text"][required]');

    requiredTextareas.forEach(function(textarea) {
        textarea.addEventListener('input', function() {
            validateField(textarea);
        });
    });

    requiredInputs.forEach(function(input) {
        input.addEventListener('input', function() {
            validateField(input);
        });
    });
});