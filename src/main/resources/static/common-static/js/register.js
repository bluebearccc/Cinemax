document.addEventListener('DOMContentLoaded', function() {

    // --- MODAL NOTIFICATION LOGIC ---

    const modalElement = document.getElementById('notificationModal');
    if (modalElement) {
        const notificationModal = new bootstrap.Modal(modalElement);
        const modalTitle = document.getElementById('modalTitle');
        const modalBody = document.getElementById('modalBody');

        // --- NEW CODE STARTS HERE ---
        // Get the close button inside the modal
        const closeButton = modalElement.querySelector('.btn-close');

        // Add a click event listener to the close button
        if (closeButton) {
            closeButton.addEventListener('click', function() {
                // Manually tell the Bootstrap modal to hide
                notificationModal.hide();
            });
        }
        // --- NEW CODE ENDS HERE ---

        const errorDiv = document.getElementById('registererror');
        const informDiv = document.getElementById('informregister');

        const showModal = (title, message, isError) => {
            modalTitle.textContent = title;
            modalBody.textContent = message;

            modalTitle.classList.toggle('text-danger', isError);
            modalTitle.classList.toggle('text-success', !isError);

            notificationModal.show();
        };

        if (errorDiv) {
            showModal('Registration Error', errorDiv.textContent, true);
        }

        if (informDiv) {
            showModal('Information', informDiv.textContent, false);
        }
    }


    // --- FORM VALIDATION LOGIC (UNCHANGED) ---

    const form = document.getElementById('registerForm');
    if (!form) return; // Stop if the form doesn't exist

    const fullNameInput = document.getElementById('fullName');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const submitBtn = document.getElementById('submitBtn');
    const passwordStrength = document.getElementById('passwordStrength');
    const passwordToggle = document.getElementById('passwordToggle');
    const confirmPasswordToggle = document.getElementById('confirmPasswordToggle');

    // SHOW/HIDE PASSWORD LOGIC
    function setupPasswordToggle(toggleButton, passwordField) {
        if (toggleButton) {
            toggleButton.addEventListener('click', function () {
                const slash = toggleButton.querySelector('.eye-slash');
                const isPassword = passwordField.type === 'password';
                passwordField.type = isPassword ? 'text' : 'password';
                slash.style.display = isPassword ? 'block' : 'none';
            });
        }
    }
    setupPasswordToggle(passwordToggle, passwordInput);
    setupPasswordToggle(confirmPasswordToggle, confirmPasswordInput);

    // VALIDATION FUNCTIONS
    function validateFullName(name) {
        const errors = [];
        if (name.length < 2) errors.push('Full name must be at least 2 characters.');
        if (name.length > 50) errors.push('Full name must be less than 50 characters.');
        if (!/^[a-zA-ZÀ-ÿ\s'-]+$/.test(name)) errors.push('Name can only contain letters and spaces.');
        return errors;
    }

    function validateEmail(email) {
        const errors = [];
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email)) errors.push('Please enter a valid email address.');
        return errors;
    }

    function validatePassword(password) {
        const errors = [];
        if (password.length < 8) errors.push('Password must be at least 8 characters.');
        if (!/(?=.*[a-z])/.test(password)) errors.push('Must contain at least one lowercase letter.');
        if (!/(?=.*[A-Z])/.test(password)) errors.push('Must contain at least one uppercase letter.');
        if (!/(?=.*\d)/.test(password)) errors.push('Must contain at least one number.');
        if (!/(?=.*[!@#$%^&*(),.?":{}|<>])/.test(password)) errors.push('Must contain at least one special character.');
        return errors;
    }

    function checkPasswordStrength(password) {
        let score = 0;
        if (password.length >= 8) score++;
        if (/(?=.*[a-z])/.test(password) && /(?=.*[A-Z])/.test(password)) score++;
        if (/(?=.*\d)/.test(password)) score++;
        if (/(?=.*[!@#$%^&*(),.?":{}|<>])/.test(password)) score++;
        if (password.length >= 12) score++;

        if (score <= 2) return 'weak';
        if (score <= 3) return 'fair';
        if (score <= 4) return 'good';
        return 'strong';
    }

    function showError(input, messages) {
        const formGroup = input.closest('.form-group, .form-group.full-width');
        const errorElement = formGroup.querySelector('.error-message');
        if (messages.length > 0) {
            formGroup.classList.add('error');
            errorElement.textContent = messages[0];
        } else {
            formGroup.classList.remove('error');
        }
    }

    function updatePasswordStrengthUI(password) {
        if (password.length > 0) {
            passwordStrength.style.display = 'block';
            const strength = checkPasswordStrength(password);
            passwordStrength.className = `password-strength strength-${strength}`;
            const strengthText = passwordStrength.querySelector('.strength-text');
            strengthText.textContent = `${strength.charAt(0).toUpperCase() + strength.slice(1)} password`;
        } else {
            passwordStrength.style.display = 'none';
        }
    }

    function checkFormValidity() {
        const fullNameValid = validateFullName(fullNameInput.value.trim()).length === 0;
        const emailValid = validateEmail(emailInput.value.trim()).length === 0;
        const passwordValid = validatePassword(passwordInput.value).length === 0;
        const confirmPasswordValid = confirmPasswordInput.value === passwordInput.value;
        submitBtn.disabled = !(fullNameValid && emailValid && passwordValid && confirmPasswordValid);
    }

    // Attach input event listeners
    [fullNameInput, emailInput, passwordInput, confirmPasswordInput].forEach(input => {
        input.addEventListener('input', () => {
            const password = passwordInput.value;
            switch(input.id) {
                case 'fullName':
                    showError(fullNameInput, validateFullName(fullNameInput.value.trim()));
                    break;
                case 'email':
                    showError(emailInput, validateEmail(emailInput.value.trim()));
                    break;
                case 'password':
                    showError(passwordInput, validatePassword(password));
                    updatePasswordStrengthUI(password);
                case 'confirmPassword':
                    const confirmErrors = password !== confirmPasswordInput.value ? ['Passwords do not match.'] : [];
                    showError(confirmPasswordInput, confirmErrors);
                    break;
            }
            checkFormValidity();
        });
    });

    // Form submission handler
    form.addEventListener('submit', function (e) {
        const fullNameErrors = validateFullName(fullNameInput.value.trim());
        const emailErrors = validateEmail(emailInput.value.trim());
        const passwordErrors = validatePassword(passwordInput.value);
        const confirmPasswordErrors = confirmPasswordInput.value !== passwordInput.value ? ['Passwords do not match.'] : [];

        const isValid = fullNameErrors.length === 0 && emailErrors.length === 0 && passwordErrors.length === 0 && confirmPasswordErrors.length === 0;

        if (!isValid) {
            e.preventDefault(); // Stop form submission if there are errors
            showError(fullNameInput, fullNameErrors);
            showError(emailInput, emailErrors);
            showError(passwordInput, passwordErrors);
            showError(confirmPasswordInput, confirmPasswordErrors);
        }
    });
});