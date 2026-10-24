/**
 * EduConnect - Login Page Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Validates inputs, coordinates with Spring Boot /api/auth/login, and handles redirects
 */

document.addEventListener('DOMContentLoaded', () => {
  // If already authenticated, redirect immediately
  if (authService.isAuthenticated()) {
    const redirectUrl = getRedirectUrl();
    window.location.href = redirectUrl;
    return;
  }

  initLoginForm();
  initDemoCredentials();
  initPasswordVisibility();
});

function getRedirectUrl() {
  const urlParams = new URLSearchParams(window.location.search);
  const redirect = urlParams.get('redirect');
  return redirect ? decodeURIComponent(redirect) : 'index.html';
}

function initPasswordVisibility() {
  const toggleBtn = document.getElementById('toggle-password-btn');
  const passwordInput = document.getElementById('login-password');
  const eyeIcon = document.getElementById('eye-icon');

  if (toggleBtn && passwordInput && eyeIcon) {
    toggleBtn.addEventListener('click', () => {
      const isPassword = passwordInput.type === 'password';
      passwordInput.type = isPassword ? 'text' : 'password';

      if (isPassword) {
        // Eye with slash icon
        eyeIcon.innerHTML = `
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18"/>
        `;
      } else {
        // Normal eye icon
        eyeIcon.innerHTML = `
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
        `;
      }
    });
  }
}

function initDemoCredentials() {
  const studentBtn = document.getElementById('demo-student-fill');
  const adminBtn = document.getElementById('demo-admin-fill');
  const emailInput = document.getElementById('login-email');
  const passwordInput = document.getElementById('login-password');

  if (studentBtn && emailInput && passwordInput) {
    studentBtn.addEventListener('click', () => {
      emailInput.value = CONFIG.DEMO_ACCOUNTS.STUDENT.email;
      passwordInput.value = CONFIG.DEMO_ACCOUNTS.STUDENT.password;
      clearErrors();
    });
  }

  if (adminBtn && emailInput && passwordInput) {
    adminBtn.addEventListener('click', () => {
      emailInput.value = CONFIG.DEMO_ACCOUNTS.ADMIN.email;
      passwordInput.value = CONFIG.DEMO_ACCOUNTS.ADMIN.password;
      clearErrors();
    });
  }
}

function clearErrors() {
  const emailErr = document.getElementById('email-error');
  const passErr = document.getElementById('password-error');
  const alertBox = document.getElementById('auth-alert');

  if (emailErr) emailErr.style.display = 'none';
  if (passErr) passErr.style.display = 'none';
  if (alertBox) alertBox.style.display = 'none';

  document.querySelectorAll('.form-control').forEach(el => el.classList.remove('is-invalid'));
}

function initLoginForm() {
  const form = document.getElementById('login-form');
  const emailInput = document.getElementById('login-email');
  const passwordInput = document.getElementById('login-password');
  const submitBtn = document.getElementById('login-submit-btn');
  const emailErr = document.getElementById('email-error');
  const passErr = document.getElementById('password-error');
  const alertBox = document.getElementById('auth-alert');

  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearErrors();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    let hasError = false;

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      emailErr.textContent = 'Please enter your college email address.';
      emailErr.style.display = 'block';
      emailInput.classList.add('is-invalid');
      hasError = true;
    } else if (!emailRegex.test(email)) {
      emailErr.textContent = 'Please enter a valid email address format.';
      emailErr.style.display = 'block';
      emailInput.classList.add('is-invalid');
      hasError = true;
    }

    // Password validation
    if (!password) {
      passErr.textContent = 'Please enter your password.';
      passErr.style.display = 'block';
      passwordInput.classList.add('is-invalid');
      hasError = true;
    }

    if (hasError) return;

    // Loading State
    submitBtn.disabled = true;
    submitBtn.innerHTML = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="animation: spin 1s linear infinite;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
      <span>Authenticating...</span>
    `;

    try {
      const result = await authService.login(email, password);

      toast.success('Welcome Back!', `Signed in as ${result.user.fullName || result.user.email}`);

      // Small pause for toast display
      setTimeout(() => {
        window.location.href = getRedirectUrl();
      }, 500);

    } catch (err) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<span>Sign In</span>';

      if (alertBox) {
        alertBox.style.display = 'block';
        alertBox.style.backgroundColor = 'var(--color-danger-bg)';
        alertBox.style.color = 'var(--color-danger-text)';
        alertBox.style.border = '1px solid var(--color-danger-border)';
        alertBox.textContent = err.message || 'Invalid email or password. Please check your credentials and try again.';
      }
    }
  });
}
