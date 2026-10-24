/**
 * EduConnect - Resource Library Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Comprehensive search, multi-filter criteria, URL state sync, and pagination
 */

document.addEventListener('DOMContentLoaded', () => {
  initResourceLibrary();
});

function initResourceLibrary() {
  const state = {
    query: '',
    categories: [],
    departments: [],
    semesters: [],
    fileTypes: [],
    sortBy: 'recent',
    page: 1,
    pageSize: 6,
    totalPages: 1,
    totalItems: 0,
    allResources: []
  };

  const elements = {
    searchInput: document.getElementById('library-search-input'),
    sortSelect: document.getElementById('sort-by-select'),
    categoryList: document.getElementById('category-filter-list'),
    departmentList: document.getElementById('department-filter-list'),
    semesterList: document.getElementById('semester-filter-list'),
    fileTypeList: document.getElementById('filetype-filter-list'),
    activeFiltersBar: document.getElementById('active-filters-bar'),
    resultsCount: document.getElementById('results-count-text'),
    gridContainer: document.getElementById('resources-grid-container'),
    pagination: document.getElementById('resources-pagination'),
    clearAllBtn: document.getElementById('clear-all-filters-btn')
  };

  // Populate dynamic filter lists from CONFIG
  populateFilterOptions();

  // Read URL query parameters to set initial state
  readQueryParamsIntoState();

  // Attach event handlers
  setupListeners();

  // Initial fetch and render
  fetchAndRender();

  function populateFilterOptions() {
    // Categories
    if (elements.categoryList) {
      elements.categoryList.innerHTML = CONFIG.CATEGORIES.map(cat => `
        <label class="filter-checkbox-item">
          <span><input type="checkbox" name="category" value="${escapeHTML(cat.id)}"> ${escapeHTML(cat.label)}</span>
        </label>
      `).join('');
    }

    // Departments
    if (elements.departmentList) {
      elements.departmentList.innerHTML = CONFIG.DEPARTMENTS.map(dept => `
        <label class="filter-checkbox-item">
          <span><input type="checkbox" name="department" value="${escapeHTML(dept)}"> ${escapeHTML(dept)}</span>
        </label>
      `).join('');
    }

    // Semesters
    if (elements.semesterList) {
      elements.semesterList.innerHTML = CONFIG.SEMESTERS.map(sem => `
        <label class="filter-checkbox-item">
          <span><input type="checkbox" name="semester" value="${escapeHTML(sem)}"> ${escapeHTML(sem)}</span>
        </label>
      `).join('');
    }
  }

  function readQueryParamsIntoState() {
    const urlParams = new URLSearchParams(window.location.search);

    const q = urlParams.get('q');
    if (q) {
      state.query = q.trim();
      if (elements.searchInput) elements.searchInput.value = state.query;
    }

    const cat = urlParams.get('category');
    if (cat) {
      state.categories = [cat];
      const cb = elements.categoryList.querySelector(`input[value="${cat}"]`);
      if (cb) cb.checked = true;
    }

    const dept = urlParams.get('department');
    if (dept) {
      state.departments = [dept];
      const cb = elements.departmentList.querySelector(`input[value="${dept}"]`);
      if (cb) cb.checked = true;
    }

    const sem = urlParams.get('semester');
    if (sem) {
      state.semesters = [sem];
      const cb = elements.semesterList.querySelector(`input[value="${sem}"]`);
      if (cb) cb.checked = true;
    }

    const sort = urlParams.get('sort');
    if (sort && elements.sortSelect) {
      state.sortBy = sort;
      elements.sortSelect.value = sort;
    }
  }

  function syncStateToURL() {
    const params = new URLSearchParams();
    if (state.query) params.set('q', state.query);
    if (state.categories.length === 1) params.set('category', state.categories[0]);
    if (state.departments.length === 1) params.set('department', state.departments[0]);
    if (state.semesters.length === 1) params.set('semester', state.semesters[0]);
    if (state.sortBy && state.sortBy !== 'recent') params.set('sort', state.sortBy);

    const newUrl = `${window.location.pathname}${params.toString() ? '?' + params.toString() : ''}`;
    window.history.replaceState({}, '', newUrl);
  }

  function setupListeners() {
    // Search input with debounce
    let debounceTimer;
    if (elements.searchInput) {
      elements.searchInput.addEventListener('input', (e) => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
          state.query = e.target.value.trim();
          state.page = 1;
          syncStateToURL();
          fetchAndRender();
        }, 300);
      });
    }

    // Sort select
    if (elements.sortSelect) {
      elements.sortSelect.addEventListener('change', (e) => {
        state.sortBy = e.target.value;
        syncStateToURL();
        fetchAndRender();
      });
    }

    // Checkbox filters
    const filterContainer = document.getElementById('filters-form');
    if (filterContainer) {
      filterContainer.addEventListener('change', (e) => {
        if (e.target && e.target.type === 'checkbox') {
          updateFilterArraysFromDOM();
          state.page = 1;
          syncStateToURL();
          fetchAndRender();
        }
      });
    }

    // Clear all filters
    if (elements.clearAllBtn) {
      elements.clearAllBtn.addEventListener('click', () => {
        clearAllFilters();
      });
    }
  }

  function updateFilterArraysFromDOM() {
    state.categories = Array.from(document.querySelectorAll('input[name="category"]:checked')).map(el => el.value);
    state.departments = Array.from(document.querySelectorAll('input[name="department"]:checked')).map(el => el.value);
    state.semesters = Array.from(document.querySelectorAll('input[name="semester"]:checked')).map(el => el.value);
    state.fileTypes = Array.from(document.querySelectorAll('input[name="fileType"]:checked')).map(el => el.value);
  }

  function clearAllFilters() {
    state.query = '';
    state.categories = [];
    state.departments = [];
    state.semesters = [];
    state.fileTypes = [];
    state.sortBy = 'recent';
    state.page = 1;

    if (elements.searchInput) elements.searchInput.value = '';
    if (elements.sortSelect) elements.sortSelect.value = 'recent';

    document.querySelectorAll('#filters-form input[type="checkbox"]').forEach(cb => {
      cb.checked = false;
    });

    syncStateToURL();
    fetchAndRender();
  }

  async function fetchAndRender() {
    renderSkeletonCards(elements.gridContainer, 6);
    renderActiveFilterChips();

    try {
      let results = [];
      let total = 0;

      // Construct API params
      const apiParams = {
        q: state.query,
        category: state.categories.join(','),
        department: state.departments.join(','),
        semester: state.semesters.join(','),
        fileType: state.fileTypes.join(','),
        sort: state.sortBy,
        page: state.page - 1, // Spring Boot is usually 0-indexed
        size: state.pageSize
      };

      try {
        const response = await apiClient.get('/resources', apiParams);
        if (Array.isArray(response)) {
          results = response;
          total = response.length;
        } else if (response && response.content) {
          results = response.content;
          total = response.totalElements || results.length;
        }
      } catch (apiErr) {
        // Fallback: Perform local filtering on sample dataset for development/presentation
        const filtered = filterLocalSampleData();
        total = filtered.length;
        const startIndex = (state.page - 1) * state.pageSize;
        results = filtered.slice(startIndex, startIndex + state.pageSize);
      }

      state.totalItems = total;
      state.totalPages = Math.max(1, Math.ceil(total / state.pageSize));

      renderResults(results);
      renderPagination();
    } catch (err) {
      console.error('Error in fetchAndRender:', err);
      renderEmptyState(elements.gridContainer, {
        title: 'Error Loading Resources',
        message: 'Could not connect to the resources database. Please verify your connection or try again.',
        actionText: 'Retry',
        onAction: () => fetchAndRender()
      });
    }
  }

  function filterLocalSampleData() {
    // Only show approved resources in public library
    return CONFIG.SAMPLE_RESOURCES.filter(r => r.isApproved).filter(r => {
      // Query search
      if (state.query) {
        const q = state.query.toLowerCase();
        const matchTitle = (r.title || '').toLowerCase().includes(q);
        const matchSubject = (r.subject || '').toLowerCase().includes(q);
        const matchDesc = (r.description || '').toLowerCase().includes(q);
        const matchDept = (r.department || '').toLowerCase().includes(q);
        if (!matchTitle && !matchSubject && !matchDesc && !matchDept) return false;
      }

      // Categories
      if (state.categories.length > 0 && !state.categories.includes(r.category)) {
        return false;
      }

      // Departments
      if (state.departments.length > 0 && !state.departments.includes(r.department)) {
        return false;
      }

      // Semesters
      if (state.semesters.length > 0 && !state.semesters.includes(r.semester)) {
        return false;
      }

      // File types
      if (state.fileTypes.length > 0 && !state.fileTypes.includes(r.fileType)) {
        return false;
      }

      return true;
    }).sort((a, b) => {
      if (state.sortBy === 'popular') return (b.downloadCount || 0) - (a.downloadCount || 0);
      if (state.sortBy === 'title_asc') return (a.title || '').localeCompare(b.title || '');
      if (state.sortBy === 'title_desc') return (b.title || '').localeCompare(a.title || '');
      // default: recent
      return new Date(b.uploadDate || 0) - new Date(a.uploadDate || 0);
    });
  }

  function renderResults(resources) {
    if (elements.resultsCount) {
      elements.resultsCount.textContent = `Showing ${resources.length} of ${state.totalItems} available materials`;
    }

    elements.gridContainer.innerHTML = '';

    if (!resources || resources.length === 0) {
      renderEmptyState(elements.gridContainer, {
        title: 'No Matching Resources Found',
        message: 'No study materials match your active search filters. Try adjusting keywords or clearing category selections.',
        actionText: 'Clear All Filters',
        onAction: () => clearAllFilters()
      });
      return;
    }

    resources.forEach(item => {
      const card = createResourceCardElement(item);
      elements.gridContainer.appendChild(card);
    });
  }

  function renderActiveFilterChips() {
    if (!elements.activeFiltersBar) return;

    const chips = [];

    if (state.query) {
      chips.push({ label: `Search: "${state.query}"`, onRemove: () => {
        state.query = '';
        if (elements.searchInput) elements.searchInput.value = '';
        fetchAndRender();
      }});
    }

    state.categories.forEach(catId => {
      const catObj = CONFIG.CATEGORIES.find(c => c.id === catId);
      chips.push({ label: catObj ? catObj.label : catId, onRemove: () => {
        state.categories = state.categories.filter(c => c !== catId);
        const cb = elements.categoryList.querySelector(`input[value="${catId}"]`);
        if (cb) cb.checked = false;
        fetchAndRender();
      }});
    });

    state.departments.forEach(dept => {
      chips.push({ label: dept, onRemove: () => {
        state.departments = state.departments.filter(d => d !== dept);
        const cb = elements.departmentList.querySelector(`input[value="${dept}"]`);
        if (cb) cb.checked = false;
        fetchAndRender();
      }});
    });

    state.semesters.forEach(sem => {
      chips.push({ label: sem, onRemove: () => {
        state.semesters = state.semesters.filter(s => s !== sem);
        const cb = elements.semesterList.querySelector(`input[value="${sem}"]`);
        if (cb) cb.checked = false;
        fetchAndRender();
      }});
    });

    if (chips.length === 0) {
      elements.activeFiltersBar.style.display = 'none';
      elements.activeFiltersBar.innerHTML = '';
      return;
    }

    elements.activeFiltersBar.style.display = 'flex';
    elements.activeFiltersBar.innerHTML = '';

    chips.forEach(chip => {
      const tag = document.createElement('span');
      tag.className = 'active-filter-tag';
      tag.innerHTML = `
        <span>${escapeHTML(chip.label)}</span>
        <button type="button" class="active-filter-remove" aria-label="Remove filter ${escapeHTML(chip.label)}">&times;</button>
      `;
      tag.querySelector('.active-filter-remove').addEventListener('click', chip.onRemove);
      elements.activeFiltersBar.appendChild(tag);
    });
  }

  function renderPagination() {
    if (!elements.pagination) return;

    if (state.totalPages <= 1) {
      elements.pagination.style.display = 'none';
      return;
    }

    elements.pagination.style.display = 'flex';
    elements.pagination.innerHTML = '';

    // Previous Page Button
    const prevBtn = document.createElement('button');
    prevBtn.type = 'button';
    prevBtn.className = 'pagination-btn';
    prevBtn.disabled = state.page === 1;
    prevBtn.innerHTML = '&larr; Prev';
    prevBtn.addEventListener('click', () => {
      if (state.page > 1) {
        state.page--;
        fetchAndRender();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
    elements.pagination.appendChild(prevBtn);

    // Numbered Buttons
    for (let i = 1; i <= state.totalPages; i++) {
      const pageBtn = document.createElement('button');
      pageBtn.type = 'button';
      pageBtn.className = `pagination-btn ${state.page === i ? 'is-active' : ''}`;
      pageBtn.textContent = i;
      pageBtn.addEventListener('click', () => {
        if (state.page !== i) {
          state.page = i;
          fetchAndRender();
          window.scrollTo({ top: 0, behavior: 'smooth' });
        }
      });
      elements.pagination.appendChild(pageBtn);
    }

    // Next Page Button
    const nextBtn = document.createElement('button');
    nextBtn.type = 'button';
    nextBtn.className = 'pagination-btn';
    nextBtn.disabled = state.page === state.totalPages;
    nextBtn.innerHTML = 'Next &rarr;';
    nextBtn.addEventListener('click', () => {
      if (state.page < state.totalPages) {
        state.page++;
        fetchAndRender();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
    elements.pagination.appendChild(nextBtn);
  }
}
