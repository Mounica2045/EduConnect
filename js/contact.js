/**
 * EduConnect - Contact Page Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Validates inquiries and interfaces honestly with the Spring Boot /api/contact endpoint
 */

document.addEventListener('DOMContentLoaded', () => {
  initContactForm();
});

function initContactForm() {
  const form = document.getElementById('contact-form');
  const submitBtn = document.getElementById('contact-submit-btn');

  const nameInput = document.getElementById('contact-name');
  const emailInput = document.getElementById('contact-email');
  const subjectSelect = document.getElementById('contact-subject');
  const messageInput = document.getElementById('contact-message');
  const alertBox = document.getElementById('contact-status-alert');

  if (!form) return;

  // Auto-fill details if student is already logged in
  if (authService.isAuthenticated()) {
    const user = authService.getCurrentUser() || {};
    if (user.fullName && !nameInput.value) nameInput.value = user.fullName;
    if (user.email && !emailInput.value) emailInput.value = user.email;
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Clear previous errors
    document.querySelectorAll('.field-error-msg').forEach(el => el.style.display = 'none');
    document.querySelectorAll('.form-control').forEach(el => el.classList.remove('is-invalid'));
    if (alertBox) alertBox.style.display = 'none';

    let hasError = false;

    // Validate Name
    const name = nameInput.value.trim();
    if (!name || name.length < 2) {
      showError('contact-name-error', nameInput, 'Please enter your name.');
      hasError = true;
    }

    // Validate Email
    const email = emailInput.value.trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email || !emailRegex.test(email)) {
      showError('contact-email-error', emailInput, 'Please provide a valid email address.');
      hasError = true;
    }

    // Validate Subject
    const subject = subjectSelect.value;
    if (!subject) {
      showError('contact-subject-error', subjectSelect, 'Please select an inquiry topic.');
      hasError = true;
    }

    // Validate Message
    const message = messageInput.value.trim();
    if (!message || message.length < 10) {
      showError('contact-message-error', messageInput, 'Please enter a message of at least 10 characters.');
      hasError = true;
    }

    if (hasError) return;

    submitBtn.disabled = true;
    submitBtn.innerHTML = `
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" style="animation: spin 1s linear infinite;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
      <span>Sending Message...</span>
    `;

    try {
      const payload = { name, email, subject, message };
      
      try {
        await apiClient.post('/contact', payload);
        // If Spring Boot endpoint is live and responded 200 OK
        if (alertBox) {
          alertBox.style.display = 'block';
          alertBox.style.backgroundColor = 'var(--color-success-bg)';
          alertBox.style.color = 'var(--color-success-text)';
          alertBox.style.border = '1px solid var(--color-success-border)';
          alertBox.textContent = 'Your message has been sent to the EduConnect team. Thank you for your feedback!';
        }
        form.reset();
      } catch (apiErr) {
        // Honest development notice:
        // As per CSP requirement: If no contact endpoint exists, show an honest development message rather than falsely claiming that the message was sent.
        if (alertBox) {
          alertBox.style.display = 'block';
          alertBox.style.backgroundColor = 'var(--color-info-bg)';
          alertBox.style.color = 'var(--color-info-text)';
          alertBox.style.border = '1px solid var(--color-info-border)';
          alertBox.innerHTML = `
            <strong>Development Notice:</strong> The Spring Boot <code>/api/contact</code> endpoint is currently awaiting backend implementation by Member 2. Your form input was validated successfully on the frontend.
          `;
        }
      }

    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = 'Send Message';
    }
  });

  function showError(elementId, input, msg) {
    const el = document.getElementById(elementId);
    if (el) {
      el.textContent = msg;
      el.style.display = 'block';
    }
    if (input) {
      input.classList.add('is-invalid');
    }
  }
}
