/**
 * EduConnect - Student Registration Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Validates fields, password confirmation, and communicates with Spring Boot /api/auth/register
 */

document.addEventListener('DOMContentLoaded', () => {
  // If already authenticated, redirect to home
  if (authService.isAuthenticated()) {
    window.location.href = 'index.html';
    return;
  }

  populateDropdowns();
  initRegisterForm();
});

function populateDropdowns() {
  const deptSelect = document.getElementById('reg-dept');
  const semSelect = document.getElementById('reg-sem');

  if (deptSelect) {
    CONFIG.DEPARTMENTS.forEach(dept => {
      const opt = document.createElement('option');
      opt.value = dept;
      opt.textContent = dept;
      deptSelect.appendChild(opt);
    });
  }

  if (semSelect) {
    CONFIG.SEMESTERS.forEach(sem => {
      const opt = document.createElement('option');
      opt.value = sem;
      opt.textContent = sem;
      semSelect.appendChild(opt);
    });
  }
}

function clearValidationErrors() {
  const errors = document.querySelectorAll('.field-error-msg');
  errors.forEach(el => el.style.display = 'none');
  document.querySelectorAll('.form-control').forEach(el => el.classList.remove('is-invalid'));
  const alertBox = document.getElementById('register-alert');
  if (alertBox) alertBox.style.display = 'none';
}

function initRegisterForm() {
  const form = document.getElementById('register-form');
  const submitBtn = document.getElementById('register-submit-btn');

  const nameInput = document.getElementById('reg-name');
  const emailInput = document.getElementById('reg-email');
  const deptSelect = document.getElementById('reg-dept');
  const semSelect = document.getElementById('reg-sem');
  const passInput = document.getElementById('reg-password');
  const confirmPassInput = document.getElementById('reg-confirm-password');
  const termsCheckbox = document.getElementById('reg-terms');
  const alertBox = document.getElementById('register-alert');

  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearValidationErrors();

    let hasError = false;

    // Name validation
    const fullName = nameInput.value.trim();
    if (!fullName || fullName.length < 2) {
      showError('name-error', nameInput, 'Please enter your full name (at least 2 characters).');
      hasError = true;
    }

    // Email validation
    const email = emailInput.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email || !emailRegex.test(email)) {
      showError('email-error', emailInput, 'Please provide a valid college email address.');
      hasError = true;
    }

    // Department validation
    const department = deptSelect.value;
    if (!department) {
      showError('dept-error', deptSelect, 'Please select your academic department.');
      hasError = true;
    }

    // Semester validation
    const semester = semSelect.value;
    if (!semester) {
      showError('sem-error', semSelect, 'Please select your current semester.');
      hasError = true;
    }

    // Password validation
    const password = passInput.value;
    if (!password || password.length < 8) {
      showError('password-error', passInput, 'Password must be at least 8 characters long.');
      hasError = true;
    }

    // Confirm password match
    const confirmPassword = confirmPassInput.value;
    if (password !== confirmPassword) {
      showError('confirm-password-error', confirmPassInput, 'Passwords do not match.');
      hasError = true;
    }

    // Terms acknowledgment
    if (!termsCheckbox.checked) {
      const termsErr = document.getElementById('terms-error');
      if (termsErr) {
        termsErr.textContent = 'You must agree to the academic guidelines to register.';
        termsErr.style.display = 'block';
      }
      hasError = true;
    }

    if (hasError) return;

    // Disable button and show spinner
    submitBtn.disabled = true;
    submitBtn.innerHTML = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="animation: spin 1s linear infinite;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
      <span>Creating Account...</span>
    `;

    try {
      const userData = {
        fullName,
        email,
        department,
        semester,
        password
      };

      await authService.register(userData);

      toast.success('Registration Successful!', 'Your student account has been created. Redirecting to login...');

      setTimeout(() => {
        window.location.href = 'login.html';
      }, 1500);

    } catch (err) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Create Account</span>';

      if (alertBox) {
        alertBox.style.display = 'block';
        alertBox.style.backgroundColor = 'var(--color-danger-bg)';
        alertBox.style.color = 'var(--color-danger-text)';
        alertBox.style.border = '1px solid var(--color-danger-border)';
        alertBox.textContent = err.message || 'Registration failed. The email may already be in use or the backend server is unreachable.';
      }
    }
  });

  function showError(errorElementId, inputElement, message) {
    const errorEl = document.getElementById(errorElementId);
    if (errorEl) {
      errorEl.textContent = message;
      errorEl.style.display = 'block';
    }
    if (inputElement) {
      inputElement.classList.add('is-invalid');
    }
  }
}
