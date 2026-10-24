/**
 * EduConnect - Reusable UI Components & DOM Helpers
 * Author: Senior Frontend Developer (CSP Team)
 * Adheres strictly to security (safe DOM generation, XSS prevention) and accessibility
 */

/**
 * Escapes unsafe HTML characters to prevent XSS injection
 */
function escapeHTML(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

/**
 * Formats ISO date string to student-friendly format (e.g., "15 Mar 2026")
 */
function formatDate(dateString) {
  if (!dateString) return 'Recent';
  try {
    const d = new Date(dateString);
    if (isNaN(d.getTime())) return dateString;
    return d.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric' });
  } catch (_) {
    return dateString;
  }
}

/**
 * Toast Notification System
 */
const toast = (() => {
  let container = null;

  function ensureContainer() {
    if (!container) {
      container = document.getElementById('toast-container');
      if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        container.setAttribute('aria-live', 'polite');
        container.setAttribute('aria-atomic', 'true');
        document.body.appendChild(container);
      }
    }
    return container;
  }

  function show(title, message, type = 'info', duration = 4000) {
    const c = ensureContainer();
    const item = document.createElement('div');
    item.className = `toast toast-${type}`;
    item.setAttribute('role', 'alert');

    let iconSvg = '';
    if (type === 'success') {
      iconSvg = '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>';
    } else if (type === 'error') {
      iconSvg = '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>';
    } else {
      iconSvg = '<svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>';
    }

    item.innerHTML = `
      <div class="toast-icon">${iconSvg}</div>
      <div class="toast-content">
        <div class="toast-title">${escapeHTML(title)}</div>
        <div class="toast-message">${escapeHTML(message)}</div>
      </div>
      <button type="button" class="toast-close" aria-label="Close notification">&times;</button>
    `;

    const closeBtn = item.querySelector('.toast-close');
    const dismiss = () => {
      item.classList.add('toast-hiding');
      setTimeout(() => {
        if (item.parentNode) item.parentNode.removeChild(item);
      }, 200);
    };

    closeBtn.addEventListener('click', dismiss);
    c.appendChild(item);

    if (duration > 0) {
      setTimeout(dismiss, duration);
    }
  }

  return {
    success: (title, msg, duration) => show(title, msg, 'success', duration),
    error: (title, msg, duration) => show(title, msg, 'error', duration),
    info: (title, msg, duration) => show(title, msg, 'info', duration)
  };
})();

/**
 * Accessible Modal Confirmation Dialog
 */
function confirmDialog({ title, message, confirmText = 'Confirm', cancelText = 'Cancel', isDanger = false, onConfirm }) {
  let backdrop = document.getElementById('global-confirm-modal');
  if (backdrop) backdrop.remove();

  backdrop = document.createElement('div');
  backdrop.id = 'global-confirm-modal';
  backdrop.className = 'modal-backdrop is-active';
  backdrop.setAttribute('role', 'dialog');
  backdrop.setAttribute('aria-modal', 'true');
  backdrop.setAttribute('aria-labelledby', 'modal-dialog-title');

  backdrop.innerHTML = `
    <div class="modal-dialog">
      <div class="modal-header">
        <h3 class="modal-title" id="modal-dialog-title">${escapeHTML(title)}</h3>
        <button type="button" class="modal-close-btn" id="modal-close-btn" aria-label="Close dialog">&times;</button>
      </div>
      <div class="modal-body">
        <p>${escapeHTML(message)}</p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline btn-sm" id="modal-cancel-btn">${escapeHTML(cancelText)}</button>
        <button type="button" class="btn ${isDanger ? 'btn-danger' : 'btn-primary'} btn-sm" id="modal-confirm-btn">${escapeHTML(confirmText)}</button>
      </div>
    </div>
  `;

  document.body.appendChild(backdrop);
  document.body.style.overflow = 'hidden';

  const closeDialog = () => {
    backdrop.classList.remove('is-active');
    document.body.style.overflow = '';
    setTimeout(() => backdrop.remove(), 250);
  };

  backdrop.querySelector('#modal-close-btn').addEventListener('click', closeDialog);
  backdrop.querySelector('#modal-cancel-btn').addEventListener('click', closeDialog);
  backdrop.querySelector('#modal-confirm-btn').addEventListener('click', async () => {
    closeDialog();
    if (typeof onConfirm === 'function') {
      await onConfirm();
    }
  });

  backdrop.addEventListener('click', (e) => {
    if (e.target === backdrop) closeDialog();
  });
}

/**
 * Bookmark Management Service (Synchronizes with Spring Boot API or local storage fallback)
 */
const bookmarkService = (() => {
  function getLocalBookmarks() {
    try {
      const raw = localStorage.getItem(CONFIG.STORAGE_KEYS.LOCAL_BOOKMARKS);
      return raw ? JSON.parse(raw) : [];
    } catch (_) {
      return [];
    }
  }

  function setLocalBookmarks(list) {
    localStorage.setItem(CONFIG.STORAGE_KEYS.LOCAL_BOOKMARKS, JSON.stringify(list));
    window.dispatchEvent(new CustomEvent('educonnect:bookmarks-updated'));
  }

  function isBookmarked(resourceId) {
    const list = getLocalBookmarks();
    return list.some(item => (typeof item === 'string' ? item : item.id) === resourceId);
  }

  async function toggle(resource) {
    const resourceId = resource.id;
    const isCurrentlyBookmarked = isBookmarked(resourceId);
    let list = getLocalBookmarks();

    if (isCurrentlyBookmarked) {
      list = list.filter(item => (typeof item === 'string' ? item : item.id) !== resourceId);
      setLocalBookmarks(list);

      // Inform backend if authenticated
      if (authService.isAuthenticated()) {
        try {
          await apiClient.delete(`/bookmarks/${resourceId}`);
        } catch (_) {
          // Keep local changes regardless
        }
      }
      toast.info('Bookmark Removed', 'Resource was removed from your saved bookmarks.');
      return false;
    } else {
      list.push(resource);
      setLocalBookmarks(list);

      if (authService.isAuthenticated()) {
        try {
          await apiClient.post(`/bookmarks/${resourceId}`, { resourceId });
        } catch (_) {
          // Keep local changes regardless
        }
      }
      toast.success('Resource Saved', 'Added to your bookmarks for quick reference.');
      return true;
    }
  }

  async function fetchAll() {
    if (authService.isAuthenticated()) {
      try {
        const data = await apiClient.get('/bookmarks');
        if (Array.isArray(data)) {
          setLocalBookmarks(data);
          return data;
        }
      } catch (err) {
        console.warn('Bookmarks API call failed, using cached list:', err.message);
      }
    }
    return getLocalBookmarks();
  }

  return {
    isBookmarked,
    toggle,
    fetchAll,
    getLocalBookmarks
  };
})();

/**
 * Reusable Resource Card Component (Safe DOM Generation)
 */
function createResourceCardElement(resource, onBookmarkChange = null) {
  const card = document.createElement('article');
  card.className = 'resource-card';
  card.setAttribute('data-id', resource.id);

  // Category Badge lookup
  const categoryConfig = CONFIG.CATEGORIES.find(c => c.id === resource.category) || {
    label: resource.categoryLabel || 'Academic Material',
    badgeClass: 'badge-other'
  };

  const isSaved = bookmarkService.isBookmarked(resource.id);

  // Card Header with category badge and bookmark action
  const cardHeader = document.createElement('div');
  cardHeader.className = 'resource-card-header';

  const badge = document.createElement('span');
  badge.className = `badge ${categoryConfig.badgeClass}`;
  badge.textContent = categoryConfig.label;
  cardHeader.appendChild(badge);

  const bookmarkBtn = document.createElement('button');
  bookmarkBtn.type = 'button';
  bookmarkBtn.className = `bookmark-btn ${isSaved ? 'is-active' : ''}`;
  bookmarkBtn.setAttribute('aria-label', isSaved ? 'Remove bookmark' : 'Bookmark this resource');
  bookmarkBtn.title = isSaved ? 'Remove bookmark' : 'Bookmark this resource';
  bookmarkBtn.innerHTML = `
    <svg fill="${isSaved ? 'currentColor' : 'none'}" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
    </svg>
  `;

  bookmarkBtn.addEventListener('click', async (e) => {
    e.stopPropagation();
    const newState = await bookmarkService.toggle(resource);
    bookmarkBtn.classList.toggle('is-active', newState);
    bookmarkBtn.querySelector('svg').setAttribute('fill', newState ? 'currentColor' : 'none');
    bookmarkBtn.setAttribute('aria-label', newState ? 'Remove bookmark' : 'Bookmark this resource');
    if (typeof onBookmarkChange === 'function') {
      onBookmarkChange(resource.id, newState);
    }
  });

  cardHeader.appendChild(bookmarkBtn);
  card.appendChild(cardHeader);

  // Title
  const title = document.createElement('h3');
  title.className = 'resource-title';
  title.textContent = resource.title || 'Untitled Resource';
  card.appendChild(title);

  // Metadata items (Subject, Semester, Department)
  const meta = document.createElement('div');
  meta.className = 'resource-meta';

  if (resource.subject) {
    const subItem = document.createElement('span');
    subItem.className = 'meta-item';
    subItem.textContent = resource.subject;
    meta.appendChild(subItem);
  }

  if (resource.semester) {
    const semItem = document.createElement('span');
    semItem.className = 'meta-item';
    semItem.textContent = resource.semester;
    meta.appendChild(semItem);
  }

  if (resource.department) {
    const deptItem = document.createElement('span');
    deptItem.className = 'meta-item';
    deptItem.textContent = resource.department;
    meta.appendChild(deptItem);
  }

  card.appendChild(meta);

  // Description
  const desc = document.createElement('p');
  desc.className = 'resource-description';
  desc.textContent = resource.description || 'No detailed summary provided for this resource.';
  card.appendChild(desc);

  // Card Footer
  const cardFooter = document.createElement('div');
  cardFooter.className = 'resource-card-footer';

  const fileInfo = document.createElement('div');
  fileInfo.className = 'file-type-pill';
  fileInfo.innerHTML = `
    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"/></svg>
    <span>${escapeHTML(resource.fileType || 'PDF')}</span>
    <span style="color: var(--color-gray-400); font-weight: normal; margin-left: 4px;">${escapeHTML(resource.fileSize || '')}</span>
  `;

  const detailsLink = document.createElement('a');
  detailsLink.href = `resource-details.html?id=${encodeURIComponent(resource.id)}`;
  detailsLink.className = 'btn btn-outline btn-sm';
  detailsLink.textContent = 'View Details';

  cardFooter.appendChild(fileInfo);
  cardFooter.appendChild(detailsLink);
  card.appendChild(cardFooter);

  return card;
}

/**
 * Renders Skeleton Loading Cards
 */
function renderSkeletonCards(container, count = 6) {
  if (!container) return;
  container.innerHTML = '';
  for (let i = 0; i < count; i++) {
    const sk = document.createElement('div');
    sk.className = 'skeleton-card';
    sk.innerHTML = `
      <div class="skeleton" style="height: 24px; width: 35%;"></div>
      <div class="skeleton" style="height: 20px; width: 90%; margin-top: 8px;"></div>
      <div class="skeleton" style="height: 16px; width: 60%;"></div>
      <div class="skeleton" style="height: 60px; width: 100%; margin-top: 12px;"></div>
      <div class="skeleton" style="height: 28px; width: 100%; margin-top: auto;"></div>
    `;
    container.appendChild(sk);
  }
}

/**
 * Renders Accessible Empty State
 */
function renderEmptyState(container, { title = 'No Resources Found', message = 'Try refining your search terms or clearing selected filters.', actionText, onAction }) {
  if (!container) return;
  container.innerHTML = `
    <div class="empty-state">
      <div class="empty-state-icon">
        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
      </div>
      <h3 class="empty-state-title">${escapeHTML(title)}</h3>
      <p class="empty-state-text">${escapeHTML(message)}</p>
      ${actionText ? `<button type="button" class="btn btn-primary btn-sm" id="empty-state-action-btn">${escapeHTML(actionText)}</button>` : ''}
    </div>
  `;

  if (actionText && typeof onAction === 'function') {
    const btn = container.querySelector('#empty-state-action-btn');
    if (btn) btn.addEventListener('click', onAction);
  }
}
