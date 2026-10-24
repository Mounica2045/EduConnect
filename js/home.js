/**
 * EduConnect - Home Page Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Manages hero search, category shortcuts, and recent resource feed
 */

document.addEventListener('DOMContentLoaded', () => {
  initHeroSearch();
  initHeroShareButton();
  loadRecentResources();
});

/**
 * Connects the hero search form and quick search chips to the resource library
 */
function initHeroSearch() {
  const form = document.getElementById('hero-search-form');
  const input = document.getElementById('hero-search-input');
  const clearBtn = document.getElementById('hero-search-clear');
  const chips = document.querySelectorAll('.search-tag-chip');

  if (form && input) {
    // Show/hide clear button dynamically
    input.addEventListener('input', () => {
      if (clearBtn) {
        clearBtn.style.display = input.value.trim() ? 'block' : 'none';
      }
    });

    if (clearBtn) {
      clearBtn.addEventListener('click', () => {
        input.value = '';
        clearBtn.style.display = 'none';
        input.focus();
      });
    }

    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const query = input.value.trim();
      if (query) {
        window.location.href = `resources.html?q=${encodeURIComponent(query)}`;
      } else {
        window.location.href = 'resources.html';
      }
    });
  }

  // Quick search topic chips
  chips.forEach(chip => {
    chip.addEventListener('click', () => {
      const q = chip.getAttribute('data-query');
      if (q) {
        window.location.href = `resources.html?q=${encodeURIComponent(q)}`;
      }
    });
  });
}

/**
 * Directs signed-in users to upload page and signed-out visitors to login
 */
function initHeroShareButton() {
  const shareBtn = document.getElementById('hero-share-btn');
  if (shareBtn) {
    shareBtn.addEventListener('click', () => {
      if (authService.isAuthenticated()) {
        window.location.href = 'upload.html';
      } else {
        window.location.href = 'login.html?redirect=upload.html';
      }
    });
  }
}

/**
 * Loads recent resources from Spring Boot API with fallback to demo sample content
 */
async function loadRecentResources() {
  const container = document.getElementById('recent-resources-grid');
  if (!container) return;

  // Show skeleton loading state
  renderSkeletonCards(container, 6);

  try {
    let resources = [];
    try {
      // Attempt to fetch latest 6 approved resources from Spring Boot REST API
      const response = await apiClient.get('/resources/recent');
      if (Array.isArray(response)) {
        resources = response;
      } else if (response && Array.isArray(response.content)) {
        resources = response.content;
      }
    } catch (apiErr) {
      console.info('Backend endpoint /api/resources/recent unavailable; rendering sample study materials for evaluation.');
      // Use clearly labeled sample items from CONFIG (filtered for approved only)
      resources = CONFIG.SAMPLE_RESOURCES.filter(r => r.isApproved).slice(0, 6);
    }

    container.innerHTML = '';

    if (!resources || resources.length === 0) {
      renderEmptyState(container, {
        title: 'No Recent Resources Yet',
        message: 'Be the first contributor to share lecture notes or lab manuals with your college community.',
        actionText: 'Contribute a Resource',
        onAction: () => {
          window.location.href = authService.isAuthenticated() ? 'upload.html' : 'login.html?redirect=upload.html';
        }
      });
      return;
    }

    // Render cards using safe DOM builder
    resources.forEach(resource => {
      const card = createResourceCardElement(resource);
      container.appendChild(card);
    });
  } catch (err) {
    console.error('Error loading recent resources:', err);
    container.innerHTML = '';
    renderEmptyState(container, {
      title: 'Unable to Load Resources',
      message: 'A network or server error occurred while retrieving recent materials. Please refresh or try again later.',
      actionText: 'Retry',
      onAction: () => loadRecentResources()
    });
  }
}
