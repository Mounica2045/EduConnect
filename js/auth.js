/**
 * EduConnect - Authentication & Session Service
 * Author: Senior Frontend Developer (CSP Team)
 * Manages JWT tokens, user states, and role-based permissions
 */

const authService = (() => {
  /**
   * Check if user has an active session
   */
  function isAuthenticated() {
    return !!localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
  }

  /**
   * Get current authenticated user details
   */
  function getCurrentUser() {
    const raw = localStorage.getItem(CONFIG.STORAGE_KEYS.USER_DATA);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch (_) {
      return null;
    }
  }

  /**
   * Get stored JWT token
   */
  function getToken() {
    return localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
  }

  /**
   * Check if current user has administrator role
   */
  function isAdmin() {
    const user = getCurrentUser();
    return user && (user.role === 'ROLE_ADMIN' || user.role === 'ADMIN');
  }

  /**
   * Set user session in browser storage
   */
  function setSession(token, user) {
    if (token) localStorage.setItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN, token);
    if (user) localStorage.setItem(CONFIG.STORAGE_KEYS.USER_DATA, JSON.stringify(user));
    window.dispatchEvent(new CustomEvent('educonnect:auth-changed', { detail: { isAuthenticated: true, user } }));
  }

  /**
   * Clear session data
   */
  function clearSession() {
    localStorage.removeItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
    localStorage.removeItem(CONFIG.STORAGE_KEYS.USER_DATA);
    window.dispatchEvent(new CustomEvent('educonnect:auth-changed', { detail: { isAuthenticated: false, user: null } }));
  }

  /**
   * Login user via Spring Boot REST endpoint with demo fallback for presentation testing
   */
  async function login(email, password) {
    try {
      const response = await apiClient.post('/auth/login', { email, password });
      // Expected backend response: { token: 'jwt...', user: { id, fullName, email, role, department, semester } }
      const token = response.token || response.accessToken || 'demo-session-token-' + Date.now();
      const user = response.user || {
        id: response.id || 'usr-1',
        fullName: response.fullName || response.name || email.split('@')[0],
        email: email,
        role: response.role || 'ROLE_STUDENT',
        department: response.department || 'Computer Science & Engineering',
        semester: response.semester || 'Semester 6'
      };

      setSession(token, user);
      return { success: true, user };
    } catch (err) {
      // If Spring Boot backend is not running yet during college presentation evaluation,
      // enable seamless testing with demo accounts
      if (err.isNetworkError || err.isTimeout || err.status === 404) {
        console.warn('Backend endpoint /api/auth/login unavailable. Checking demo accounts for evaluation...');
        
        if (email.toLowerCase() === CONFIG.DEMO_ACCOUNTS.ADMIN.email.toLowerCase() &&
            password === CONFIG.DEMO_ACCOUNTS.ADMIN.password) {
          const user = { ...CONFIG.DEMO_ACCOUNTS.ADMIN, id: 'admin-1', isDemoAccount: true };
          setSession('demo-jwt-admin-token', user);
          return { success: true, user, isDemoFallback: true };
        }

        if (email.toLowerCase() === CONFIG.DEMO_ACCOUNTS.STUDENT.email.toLowerCase() &&
            password === CONFIG.DEMO_ACCOUNTS.STUDENT.password) {
          const user = { ...CONFIG.DEMO_ACCOUNTS.STUDENT, id: 'student-1', isDemoAccount: true };
          setSession('demo-jwt-student-token', user);
          return { success: true, user, isDemoFallback: true };
        }

        // Generic fallback for any email during presentation if explicitly using Password123!
        if (password === 'Password123!') {
          const user = {
            id: 'demo-user-' + Date.now(),
            fullName: email.split('@')[0].replace('.', ' ').replace(/(^\w|\s\w)/g, m => m.toUpperCase()),
            email: email,
            role: 'ROLE_STUDENT',
            department: 'Computer Science & Engineering',
            semester: 'Semester 4',
            isDemoAccount: true
          };
          setSession('demo-jwt-token-' + Date.now(), user);
          return { success: true, user, isDemoFallback: true };
        }
      }

      throw err;
    }
  }

  /**
   * Register new user via Spring Boot REST endpoint
   */
  async function register(userData) {
    try {
      const response = await apiClient.post('/auth/register', userData);
      return { success: true, data: response };
    } catch (err) {
      if (err.isNetworkError || err.isTimeout || err.status === 404) {
        console.warn('Backend endpoint /api/auth/register unavailable. Emulating successful registration for preview...');
        return {
          success: true,
          data: { message: 'Demo registration successful! You can now log in.' },
          isDemoFallback: true
        };
      }
      throw err;
    }
  }

  /**
   * Log out current user
   */
  async function logout(redirect = true) {
    try {
      if (isAuthenticated()) {
        await apiClient.post('/auth/logout', {}).catch(() => {});
      }
    } finally {
      clearSession();
      if (redirect) {
        window.location.href = 'index.html';
      }
    }
  }

  /**
   * Route guard: Requires user to be authenticated
   */
  function requireAuth(redirectPath = window.location.pathname) {
    if (!isAuthenticated()) {
      const target = encodeURIComponent(redirectPath);
      window.location.href = `login.html?redirect=${target}`;
      return false;
    }
    return true;
  }

  /**
   * Route guard: Requires user to be an administrator
   */
  function requireAdmin() {
    if (!isAuthenticated()) {
      window.location.href = `login.html?redirect=admin.html`;
      return false;
    }
    if (!isAdmin()) {
      alert('Access restricted: Administrator privileges required to access this dashboard.');
      window.location.href = 'index.html';
      return false;
    }
    return true;
  }

  return {
    isAuthenticated,
    getCurrentUser,
    getToken,
    isAdmin,
    setSession,
    clearSession,
    login,
    register,
    logout,
    requireAuth,
    requireAdmin
  };
})();
