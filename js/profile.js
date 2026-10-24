/**
 * EduConnect - Student Profile Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Displays user academic info, stats, uploaded contributions, and profile edits
 */

document.addEventListener('DOMContentLoaded', () => {
  if (!authService.requireAuth('profile.html')) {
    return;
  }

  initProfilePage();
});

function initProfilePage() {
  const user = authService.getCurrentUser() || {};

  // Elements
  const avatarCircle = document.getElementById('profile-avatar-circle');
  const displayName = document.getElementById('profile-display-name');
  const displayEmail = document.getElementById('profile-display-email');
  const roleBadge = document.getElementById('profile-role-badge');
  const uploadsCount = document.getElementById('stat-uploads-count');
  const bookmarksCount = document.getElementById('stat-bookmarks-count');
  const logoutBtn = document.getElementById('profile-logout-btn');

  // Populate basic profile data
  const initial = (user.fullName || user.email || 'U').charAt(0).toUpperCase();
  if (avatarCircle) avatarCircle.textContent = initial;
  if (displayName) displayName.textContent = user.fullName || 'Student User';
  if (displayEmail) displayEmail.textContent = user.email || 'student@college.edu';

  if (roleBadge) {
    if (authService.isAdmin()) {
      roleBadge.textContent = 'Administrator / Faculty Coordinator';
      roleBadge.style.backgroundColor = 'var(--cat-qp-bg)';
      roleBadge.style.color = 'var(--cat-qp)';
      roleBadge.style.borderColor = '#ddd6fe';
    } else {
      roleBadge.textContent = `${user.department || 'Engineering'} • ${user.semester || 'Student'}`;
    }
  }

  // Count bookmarks
  const bookmarks = bookmarkService.getLocalBookmarks();
  if (bookmarksCount) bookmarksCount.textContent = bookmarks.length;

  // Logout button
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      confirmDialog({
        title: 'Sign Out Confirmation',
        message: 'Are you sure you want to end your current EduConnect session?',
        confirmText: 'Sign Out',
        isDanger: true,
        onConfirm: () => authService.logout()
      });
    });
  }

  // Setup tabs
  initProfileTabs();

  // Setup edit form
  initEditProfileForm(user);

  // Load user uploaded resources
  loadUserUploads(user, uploadsCount);
}

function initProfileTabs() {
  const tabBtnUploads = document.getElementById('tab-btn-uploads');
  const tabBtnDetails = document.getElementById('tab-btn-details');
  const tabContentUploads = document.getElementById('tab-content-uploads');
  const tabContentDetails = document.getElementById('tab-content-details');

  if (tabBtnUploads && tabBtnDetails && tabContentUploads && tabContentDetails) {
    tabBtnUploads.addEventListener('click', () => {
      tabBtnUploads.classList.add('is-active');
      tabBtnUploads.setAttribute('aria-selected', 'true');
      tabBtnDetails.classList.remove('is-active');
      tabBtnDetails.setAttribute('aria-selected', 'false');

      tabContentUploads.style.display = 'block';
      tabContentDetails.style.display = 'none';
    });

    tabBtnDetails.addEventListener('click', () => {
      tabBtnDetails.classList.add('is-active');
      tabBtnDetails.setAttribute('aria-selected', 'true');
      tabBtnUploads.classList.remove('is-active');
      tabBtnUploads.setAttribute('aria-selected', 'false');

      tabContentDetails.style.display = 'block';
      tabContentUploads.style.display = 'none';
    });
  }
}

function initEditProfileForm(user) {
  const form = document.getElementById('edit-profile-form');
  const nameInput = document.getElementById('edit-name');
  const emailInput = document.getElementById('edit-email');
  const deptSelect = document.getElementById('edit-dept');
  const semSelect = document.getElementById('edit-sem');

  if (!form) return;

  // Populate options
  if (deptSelect) {
    CONFIG.DEPARTMENTS.forEach(dept => {
      const opt = document.createElement('option');
      opt.value = dept;
      opt.textContent = dept;
      if (dept === user.department) opt.selected = true;
      deptSelect.appendChild(opt);
    });
  }

  if (semSelect) {
    CONFIG.SEMESTERS.forEach(sem => {
      const opt = document.createElement('option');
      opt.value = sem;
      opt.textContent = sem;
      if (sem === user.semester) opt.selected = true;
      semSelect.appendChild(opt);
    });
  }

  if (nameInput) nameInput.value = user.fullName || '';
  if (emailInput) emailInput.value = user.email || '';

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const updatedUser = {
      ...user,
      fullName: nameInput.value.trim(),
      department: deptSelect.value,
      semester: semSelect.value
    };

    try {
      try {
        await apiClient.put('/users/me', updatedUser);
      } catch (_) {
        // Fallback for offline demo
      }

      authService.setSession(authService.getToken(), updatedUser);
      toast.success('Profile Updated', 'Your profile details have been successfully saved.');

      // Update header & sidebar text
      const displayName = document.getElementById('profile-display-name');
      if (displayName) displayName.textContent = updatedUser.fullName;
    } catch (err) {
      toast.error('Update Failed', err.message || 'Could not save profile changes.');
    }
  });
}

async function loadUserUploads(user, countBadge) {
  const container = document.getElementById('user-uploads-container');
  if (!container) return;

  // Filter sample resources where contributor is current user or matching name
  const userName = (user.fullName || '').toLowerCase();
  const userUploads = CONFIG.SAMPLE_RESOURCES.filter(r => {
    return (r.contributorName || '').toLowerCase() === userName ||
           (r.contributorName === 'Aarav Sharma' && user.role === 'ROLE_STUDENT');
  });

  if (countBadge) countBadge.textContent = userUploads.length;

  container.innerHTML = '';

  if (userUploads.length === 0) {
    renderEmptyState(container, {
      title: 'No Uploaded Materials Yet',
      message: 'You have not uploaded any study resources to EduConnect. Help your classmates by sharing lecture notes or question papers.',
      actionText: 'Upload Your First Resource',
      onAction: () => { window.location.href = 'upload.html'; }
    });
    return;
  }

  // Render Table of User Uploads
  const tableWrapper = document.createElement('div');
  tableWrapper.className = 'table-responsive';

  const table = document.createElement('table');
  table.className = 'data-table';
  table.innerHTML = `
    <thead>
      <tr>
        <th>Document Title</th>
        <th>Category</th>
        <th>Subject</th>
        <th>Upload Date</th>
        <th>Status</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      ${userUploads.map(res => `
        <tr>
          <td>
            <strong>${escapeHTML(res.title)}</strong>
            <div style="font-size: 0.75rem; color: var(--color-gray-500);">${escapeHTML(res.fileType || 'PDF')} &bull; ${escapeHTML(res.fileSize || '')}</div>
          </td>
          <td><span class="badge ${res.category === 'notes' ? 'badge-notes' : 'badge-lab'}">${escapeHTML(res.categoryLabel || res.category)}</span></td>
          <td>${escapeHTML(res.subject || 'General')}</td>
          <td>${formatDate(res.uploadDate)}</td>
          <td>
            <span class="badge ${res.isApproved ? 'badge-status-approved' : 'badge-status-pending'}">
              ${res.isApproved ? 'Approved & Published' : 'Pending Verification'}
            </span>
          </td>
          <td>
            <a href="resource-details.html?id=${encodeURIComponent(res.id)}" class="btn btn-outline btn-sm">View</a>
          </td>
        </tr>
      `).join('')}
    </tbody>
  `;

  tableWrapper.appendChild(table);
  container.appendChild(tableWrapper);
}
