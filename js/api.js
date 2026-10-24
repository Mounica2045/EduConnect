/**
 * EduConnect - Reusable Fetch API Client
 * Author: Senior Frontend Developer (CSP Team)
 * Communicates with Spring Boot REST Controllers
 */

const apiClient = (() => {
  /**
   * Internal helper to build URL with query params
   */
  function buildUrl(endpoint, params = {}) {
    const url = new URL(
      endpoint.startsWith('http') ? endpoint : `${CONFIG.API_BASE_URL}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`
    );
    Object.keys(params).forEach(key => {
      if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
        url.searchParams.append(key, params[key]);
      }
    });
    return url.toString();
  }

  /**
   * Internal helper to retrieve auth headers
   */
  function getHeaders(isFormData = false) {
    const headers = {};
    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
    }
    headers['Accept'] = 'application/json';

    const token = localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN);
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    return headers;
  }

  /**
   * Generic request handler with timeout and error handling
   */
  async function request(endpoint, options = {}, params = {}) {
    const url = buildUrl(endpoint, params);
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), CONFIG.REQUEST_TIMEOUT_MS);

    const config = {
      ...options,
      signal: controller.signal
    };

    try {
      const response = await fetch(url, config);
      clearTimeout(timeoutId);

      // Handle HTTP Error responses
      if (!response.ok) {
        let errorData = null;
        try {
          errorData = await response.json();
        } catch (_) {
          // Response body is not JSON
        }

        const error = new Error(
          (errorData && (errorData.message || errorData.error)) ||
          `Request failed with status ${response.status} (${response.statusText})`
        );
        error.status = response.status;
        error.data = errorData;

        // Auto-logout on 401 Unauthorized if token was present
        if (response.status === 401 && localStorage.getItem(CONFIG.STORAGE_KEYS.AUTH_TOKEN)) {
          authService.logout(false);
        }

        throw error;
      }

      // Check if response has content to parse
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      }

      return await response.text();
    } catch (err) {
      clearTimeout(timeoutId);

      if (err.name === 'AbortError') {
        const timeoutError = new Error('The request timed out. Please check if the Spring Boot server is reachable.');
        timeoutError.isTimeout = true;
        throw timeoutError;
      }

      // Re-throw with network flag if connection failed
      if (err instanceof TypeError && err.message.includes('fetch')) {
        err.isNetworkError = true;
      }

      throw err;
    }
  }

  return {
    /**
     * HTTP GET request
     */
    get(endpoint, params = {}) {
      return request(endpoint, {
        method: 'GET',
        headers: getHeaders(false)
      }, params);
    },

    /**
     * HTTP POST request
     */
    post(endpoint, data = {}, params = {}) {
      return request(endpoint, {
        method: 'POST',
        headers: getHeaders(false),
        body: JSON.stringify(data)
      }, params);
    },

    /**
     * HTTP PUT request
     */
    put(endpoint, data = {}, params = {}) {
      return request(endpoint, {
        method: 'PUT',
        headers: getHeaders(false),
        body: JSON.stringify(data)
      }, params);
    },

    /**
     * HTTP PATCH request
     */
    patch(endpoint, data = {}, params = {}) {
      return request(endpoint, {
        method: 'PATCH',
        headers: getHeaders(false),
        body: JSON.stringify(data)
      }, params);
    },

    /**
     * HTTP DELETE request
     */
    delete(endpoint, params = {}) {
      return request(endpoint, {
        method: 'DELETE',
        headers: getHeaders(false)
      }, params);
    },

    /**
     * Multipart file upload (FormData)
     * NOTE: Browser automatically sets Content-Type with boundary when omitting Content-Type
     */
    upload(endpoint, formData, params = {}) {
      return request(endpoint, {
        method: 'POST',
        headers: getHeaders(true),
        body: formData
      }, params);
    }
  };
})();
