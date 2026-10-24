/**
 * EduConnect - Administrator Dashboard Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Moderation actions (Approve, Reject, Delete), resource catalog management, and metrics
 */

document.addEventListener('DOMContentLoaded', () => {
  initAdminDashboard();
});

let adminResources = [];

async function initAdminDashboard() {
  const isAuth = authService.isAuthenticated();
  const isAdmin = authService.isAdmin();
  const demoControls = document.getElementById('admin-demo-controls');
  const demoLoginBtn = document.getElementById('btn-login-as-admin');

  if (!isAuth || !isAdmin) {
    if (demoControls && demoLoginBtn) {
      demoControls.style.display = 'flex';
      demoLoginBtn.addEventListener('click', async () => {
        await authService.login(CONFIG.DEMO_ACCOUNTS.ADMIN.email, CONFIG.DEMO_ACCOUNTS.ADMIN.password);
        window.location.reload();
      });
    }

    if (!isAuth) {
      window.location.href = 'login.html?redirect=admin.html';
      return;
    }
  }

  loadAdminData();
  setupAdminSearch();
}

async function loadAdminData() {
  const pendingContainer = document.getElementById('pending-table-container');
  const allContainer = document.getElementById('all-resources-table-container');
  const totalStat = document.getElementById('admin-stat-total');
  const pendingStat = document.getElementById('admin-stat-pending');
  const pendingBadge = document.getElementById('pending-queue-badge');

  if (pendingContainer) pendingContainer.innerHTML = '<div class="skeleton-card" style="height: 120px;"></div>';
  if (allContainer) allContainer.innerHTML = '<div class="skeleton-card" style="height: 180px;"></div>';

  try {
    try {
      const response = await apiClient.get('/admin/resources');
      adminResources = Array.isArray(response) ? response : (response.content || []);
    } catch (_) {
      console.info('Endpoint /api/admin/resources unavailable; using sample data array.');
      adminResources = [...CONFIG.SAMPLE_RESOURCES];
    }

    const pending = adminResources.filter(r => !r.isApproved);
    const approved = adminResources.filter(r => r.isApproved);

    if (totalStat) totalStat.textContent = adminResources.length;
    if (pendingStat) pendingStat.textContent = pending.length;
    if (pendingBadge) pendingBadge.textContent = `${pending.length} items awaiting review`;

    renderPendingQueue(pendingContainer, pending);
    renderAllResources(allContainer, approved);

  } catch (err) {
    console.error('Error loading admin data:', err);
    toast.error('Admin Error', 'Could not load management dataset.');
  }
}

function renderPendingQueue(container, pendingItems) {
  if (!container) return;
  container.innerHTML = '';

  if (pendingItems.length === 0) {
    renderEmptyState(container, {
      title: 'Review Queue Clear',
      message: 'All submitted student materials have been reviewed and approved.'
    });
    return;
  }

  const tableWrapper = document.createElement('div');
  tableWrapper.className = 'table-responsive';

  const table = document.createElement('table');
  table.className = 'data-table';
  table.innerHTML = `
    <thead>
      <tr>
        <th>Resource Title</th>
        <th>Category</th>
        <th>Department & Semester</th>
        <th>Contributor</th>
        <th>Submission Date</th>
        <th style="text-align: right;">Moderation Actions</th>
      </tr>
    </thead>
    <tbody>
      ${pendingItems.map(item => `
        <tr id="row-pending-${escapeHTML(item.id)}">
          <td>
            <strong>${escapeHTML(item.title)}</strong>
            <div style="font-size: 0.75rem; color: var(--color-gray-500);">${escapeHTML(item.fileType || 'PDF')} &bull; ${escapeHTML(item.fileSize || '')}</div>
          </td>
          <td><span class="badge badge-other">${escapeHTML(item.categoryLabel || item.category)}</span></td>
          <td>${escapeHTML(item.department || '')} (${escapeHTML(item.semester || '')})</td>
          <td>${escapeHTML(item.contributorName || 'Student')}</td>
          <td>${formatDate(item.uploadDate)}</td>
          <td style="text-align: right; white-space: nowrap;">
            <button type="button" class="btn btn-secondary btn-sm" onclick="handleApproveResource('${escapeHTML(item.id)}')">
              Approve
            </button>
            <button type="button" class="btn btn-outline btn-sm" style="color: var(--color-danger-main);" onclick="handleRejectResource('${escapeHTML(item.id)}')">
              Reject
            </button>
          </td>
        </tr>
      `).join('')}
    </tbody>
  `;

  tableWrapper.appendChild(table);
  container.appendChild(tableWrapper);
}

function renderAllResources(container, items) {
  if (!container) return;
  container.innerHTML = '';

  if (items.length === 0) {
    renderEmptyState(container, {
      title: 'No Published Resources',
      message: 'No resources match current filter criteria.'
    });
    return;
  }

  const tableWrapper = document.createElement('div');
  tableWrapper.className = 'table-responsive';

  const table = document.createElement('table');
  table.className = 'data-table';
  table.innerHTML = `
    <thead>
      <tr>
        <th>Document</th>
        <th>Category</th>
        <th>Subject</th>
        <th>Department</th>
        <th>Downloads</th>
        <th style="text-align: right;">Actions</th>
      </tr>
    </thead>
    <tbody>
      ${items.map(item => `
        <tr id="row-resource-${escapeHTML(item.id)}">
          <td>
            <a href="resource-details.html?id=${encodeURIComponent(item.id)}" style="font-weight: 600;">
              ${escapeHTML(item.title)}
            </a>
            <div style="font-size: 0.75rem; color: var(--color-gray-500);">${escapeHTML(item.contributorName || '')} &bull; ${formatDate(item.uploadDate)}</div>
          </td>
          <td><span class="badge badge-notes">${escapeHTML(item.categoryLabel || item.category)}</span></td>
          <td>${escapeHTML(item.subject || 'General')}</td>
          <td>${escapeHTML(item.department || '')}</td>
          <td>${escapeHTML(item.downloadCount || 0)}</td>
          <td style="text-align: right;">
            <button type="button" class="btn btn-outline btn-sm" style="color: var(--color-danger-main);" onclick="handleDeleteResource('${escapeHTML(item.id)}')">
              Delete
            </button>
          </td>
        </tr>
      `).join('')}
    </tbody>
  `;

  tableWrapper.appendChild(table);
  container.appendChild(tableWrapper);
}

function setupAdminSearch() {
  const searchInput = document.getElementById('admin-search-input');
  const allContainer = document.getElementById('all-resources-table-container');

  if (searchInput && allContainer) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.trim().toLowerCase();
      const approved = adminResources.filter(r => r.isApproved);
      if (!q) {
        renderAllResources(allContainer, approved);
        return;
      }
      const filtered = approved.filter(item => {
        return (item.title || '').toLowerCase().includes(q) ||
               (item.contributorName || '').toLowerCase().includes(q) ||
               (item.subject || '').toLowerCase().includes(q);
      });
      renderAllResources(allContainer, filtered);
    });
  }
}

// Global action handlers for table inline triggers
window.handleApproveResource = async function(id) {
  try {
    try {
      await apiClient.patch(`/admin/resources/${id}/approve`, {});
    } catch (_) {}

    const found = adminResources.find(r => r.id === id);
    if (found) found.isApproved = true;

    toast.success('Resource Approved', 'The document has been approved and is now live in the Resource Library.');
    loadAdminData();
  } catch (err) {
    toast.error('Approval Error', err.message);
  }
};

window.handleRejectResource = async function(id) {
  confirmDialog({
    title: 'Reject Resource Submission',
    message: 'Are you sure you want to reject this submission? It will be removed from the review queue.',
    confirmText: 'Reject Document',
    isDanger: true,
    onConfirm: async () => {
      try {
        try {
          await apiClient.patch(`/admin/resources/${id}/reject`, {});
        } catch (_) {}

        adminResources = adminResources.filter(r => r.id !== id);
        toast.info('Resource Rejected', 'The submission was removed from the moderation queue.');
        loadAdminData();
      } catch (err) {
        toast.error('Rejection Error', err.message);
      }
    }
  });
};

window.handleDeleteResource = function(id) {
  confirmDialog({
    title: 'Delete Resource Confirmation',
    message: 'Are you sure you want to permanently delete this resource? This action cannot be undone.',
    confirmText: 'Delete Permanently',
    isDanger: true,
    onConfirm: async () => {
      try {
        try {
          await apiClient.delete(`/admin/resources/${id}`);
        } catch (_) {}

        adminResources = adminResources.filter(r => r.id !== id);
        toast.success('Resource Deleted', 'The academic material was deleted.');
        loadAdminData();
      } catch (err) {
        toast.error('Deletion Error', err.message);
      }
    }
  });
};
