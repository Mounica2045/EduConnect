/**
 * EduConnect - Shared Navigation & Header Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Manages responsive menu, auth state UI synchronization, and accessibility
 */

document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initHeaderScroll();
  syncAuthNavigation();

  // Listen for login/logout changes across tabs or in-page actions
  window.addEventListener('educonnect:auth-changed', () => {
    syncAuthNavigation();
  });
});

/**
 * Initializes navigation bar interactions and mobile drawer
 */
function initNavigation() {
  const toggleBtn = document.getElementById('mobile-nav-toggle');
  const drawer = document.getElementById('mobile-drawer');
  const overlay = document.getElementById('mobile-nav-overlay');
  const closeBtn = document.getElementById('mobile-drawer-close');

  if (toggleBtn && drawer && overlay) {
    const openMenu = () => {
      drawer.classList.add('is-active');
      overlay.classList.add('is-active');
      toggleBtn.setAttribute('aria-expanded', 'true');
      document.body.style.overflow = 'hidden';
      if (closeBtn) closeBtn.focus();
    };

    const closeMenu = () => {
      drawer.classList.remove('is-active');
      overlay.classList.remove('is-active');
      toggleBtn.setAttribute('aria-expanded', 'false');
      document.body.style.overflow = '';
      toggleBtn.focus();
    };

    toggleBtn.addEventListener('click', () => {
      const isExpanded = toggleBtn.getAttribute('aria-expanded') === 'true';
      if (isExpanded) {
        closeMenu();
      } else {
        openMenu();
      }
    });

    if (closeBtn) closeBtn.addEventListener('click', closeMenu);
    overlay.addEventListener('click', closeMenu);

    // Close when Esc key is pressed
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && drawer.classList.contains('is-active')) {
        closeMenu();
      }
    });

    // Close mobile menu when clicking any nav link
    const mobileLinks = drawer.querySelectorAll('.mobile-nav-link');
    mobileLinks.forEach(link => {
      link.addEventListener('click', closeMenu);
    });
  }

  // Highlight active page link based on current path
  highlightActiveLink();
}

/**
 * Automatically sets the active class on corresponding nav links
 */
function highlightActiveLink() {
  const currentPath = window.location.pathname.split('/').pop() || 'index.html';
  const navLinks = document.querySelectorAll('.nav-link, .mobile-nav-link');

  navLinks.forEach(link => {
    const href = link.getAttribute('href');
    if (href === currentPath || (currentPath === '' && href === 'index.html')) {
      link.classList.add('active');
      link.setAttribute('aria-current', 'page');
    } else {
      link.classList.remove('active');
      link.removeAttribute('aria-current');
    }
  });
}

/**
 * Syncs the header UI with the current user authentication state
 */
function syncAuthNavigation() {
  const authNavDesktop = document.getElementById('desktop-auth-container');
  const authNavMobile = document.getElementById('mobile-auth-container');

  const isAuth = authService.isAuthenticated();
  const user = authService.getCurrentUser();
  const isAdmin = authService.isAdmin();

  if (authNavDesktop) {
    if (isAuth && user) {
      const initial = (user.fullName || user.email || 'U').charAt(0).toUpperCase();
      const adminLink = isAdmin ? `
        <a href="admin.html" class="dropdown-item">
          <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/></svg>
          Admin Dashboard
        </a>` : '';

      authNavDesktop.innerHTML = `
        <div class="user-menu-wrapper" id="user-menu-wrapper">
          <button type="button" class="user-menu-btn" id="user-menu-toggle" aria-haspopup="true" aria-expanded="false" aria-label="User account menu">
            <span class="user-avatar-circle">${initial}</span>
            <span class="user-menu-name">${escapeHTML(user.fullName || user.email)}</span>
            <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/></svg>
          </button>
          <div class="dropdown-menu" id="user-dropdown-menu" role="menu">
            <div class="dropdown-header">
              <div class="dropdown-user-name">${escapeHTML(user.fullName || 'Student')}</div>
              <div class="dropdown-user-role">${escapeHTML(user.department || user.email)}</div>
            </div>
            <a href="profile.html" class="dropdown-item" role="menuitem">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              My Profile
            </a>
            <a href="bookmarks.html" class="dropdown-item" role="menuitem">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/></svg>
              Saved Bookmarks
            </a>
            <a href="upload.html" class="dropdown-item" role="menuitem">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
              Upload Resource
            </a>
            ${adminLink}
            <div class="dropdown-divider"></div>
            <button type="button" class="dropdown-item danger" id="desktop-logout-btn" role="menuitem">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
              Sign Out
            </button>
          </div>
        </div>
      `;

      // Hook up dropdown toggle
      const menuToggle = document.getElementById('user-menu-toggle');
      const dropdownMenu = document.getElementById('user-dropdown-menu');
      if (menuToggle && dropdownMenu) {
        menuToggle.addEventListener('click', (e) => {
          e.stopPropagation();
          const isOpen = dropdownMenu.classList.contains('show');
          dropdownMenu.classList.toggle('show');
          menuToggle.setAttribute('aria-expanded', !isOpen);
        });

        document.addEventListener('click', () => {
          dropdownMenu.classList.remove('show');
          menuToggle.setAttribute('aria-expanded', 'false');
        });
      }

      const logoutBtn = document.getElementById('desktop-logout-btn');
      if (logoutBtn) {
        logoutBtn.addEventListener('click', () => authService.logout());
      }
    } else {
      authNavDesktop.innerHTML = `
        <a href="login.html" class="btn btn-outline btn-sm">Sign In</a>
        <a href="register.html" class="btn btn-primary btn-sm">Join EduConnect</a>
      `;
    }
  }

  if (authNavMobile) {
    if (isAuth && user) {
      const adminMobile = isAdmin ? `
        <a href="admin.html" class="mobile-nav-link">
          <svg width="18" height="18" fill="currentColor" viewBox="0 0 24 24"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"/></svg>
          Admin Dashboard
        </a>` : '';

      authNavMobile.innerHTML = `
        <div style="padding: 8px 12px; margin-bottom: 8px; background-color: var(--color-gray-100); border-radius: var(--radius-md);">
          <div style="font-weight: 600; font-size: 0.875rem;">${escapeHTML(user.fullName || user.email)}</div>
          <div style="font-size: 0.75rem; color: var(--color-gray-500);">${escapeHTML(user.department || '')}</div>
        </div>
        <a href="profile.html" class="mobile-nav-link">
          <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
          My Profile
        </a>
        <a href="bookmarks.html" class="mobile-nav-link">
          <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/></svg>
          Saved Bookmarks
        </a>
        <a href="upload.html" class="mobile-nav-link">
          <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"/></svg>
          Upload Resource
        </a>
        ${adminMobile}
        <button type="button" class="btn btn-outline btn-sm" id="mobile-logout-btn" style="width: 100%; margin-top: 8px;">Sign Out</button>
      `;

      const mLogoutBtn = document.getElementById('mobile-logout-btn');
      if (mLogoutBtn) {
        mLogoutBtn.addEventListener('click', () => authService.logout());
      }
    } else {
      authNavMobile.innerHTML = `
        <a href="login.html" class="btn btn-outline" style="width: 100%;">Sign In</a>
        <a href="register.html" class="btn btn-primary" style="width: 100%;">Join EduConnect</a>
      `;
    }
  }
}

/**
 * Adds shadow to header when user scrolls down
 */
function initHeaderScroll() {
  const header = document.querySelector('.site-header');
  if (!header) return;

  const handleScroll = () => {
    if (window.scrollY > 10) {
      header.classList.add('is-scrolled');
    } else {
      header.classList.remove('is-scrolled');
    }
  };

  window.addEventListener('scroll', handleScroll, { passive: true });
}
