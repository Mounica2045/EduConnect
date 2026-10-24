/**
 * EduConnect - Saved Bookmarks Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Fetches bookmarked items from Spring Boot API or local cache, provides in-page search
 */

document.addEventListener('DOMContentLoaded', () => {
  initBookmarksPage();
});

async function initBookmarksPage() {
  const container = document.getElementById('bookmarks-grid-container');
  const searchInput = document.getElementById('bookmark-filter-input');
  const guestNotice = document.getElementById('guest-bookmarks-notice');

  // Check if guest
  if (!authService.isAuthenticated()) {
    if (guestNotice) guestNotice.style.display = 'flex';
  }

  renderSkeletonCards(container, 3);

  let bookmarkedItems = [];

  try {
    const rawList = await bookmarkService.fetchAll();

    // Map IDs to full objects if list has IDs or objects
    bookmarkedItems = rawList.map(item => {
      if (typeof item === 'string') {
        const found = CONFIG.SAMPLE_RESOURCES.find(r => r.id === item);
        return found || { id: item, title: 'Saved Study Resource', description: 'Saved resource' };
      }
      return item;
    });

    renderList(bookmarkedItems);

  } catch (err) {
    console.error('Error fetching bookmarks:', err);
    renderEmptyState(container, {
      title: 'Unable to Load Bookmarks',
      message: 'Could not retrieve your saved materials. Please refresh or check your internet connection.',
      actionText: 'Retry',
      onAction: () => initBookmarksPage()
    });
    return;
  }

  // Filter within bookmarks
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      const q = e.target.value.trim().toLowerCase();
      if (!q) {
        renderList(bookmarkedItems);
        return;
      }
      const filtered = bookmarkedItems.filter(item => {
        return (item.title || '').toLowerCase().includes(q) ||
               (item.subject || '').toLowerCase().includes(q) ||
               (item.description || '').toLowerCase().includes(q);
      });
      renderList(filtered, true);
    });
  }

  function renderList(list, isFiltering = false) {
    container.innerHTML = '';

    if (!list || list.length === 0) {
      if (isFiltering) {
        renderEmptyState(container, {
          title: 'No Matching Saved Bookmarks',
          message: 'No saved resources match your query. Try a different search term.',
          actionText: 'Show All Saved',
          onAction: () => {
            searchInput.value = '';
            renderList(bookmarkedItems);
          }
        });
      } else {
        renderEmptyState(container, {
          title: 'No Saved Bookmarks Yet',
          message: 'You have not saved any academic resources to your bookmarks yet. Click the bookmark icon on any resource card to save it for rapid access.',
          actionText: 'Explore Resource Library',
          onAction: () => {
            window.location.href = 'resources.html';
          }
        });
      }
      return;
    }

    list.forEach(item => {
      const card = createResourceCardElement(item, (resourceId, isBookmarked) => {
        if (!isBookmarked) {
          // Remove from local array
          bookmarkedItems = bookmarkedItems.filter(b => b.id !== resourceId);
          // Animate and remove card from DOM
          card.style.opacity = '0';
          card.style.transform = 'scale(0.9)';
          setTimeout(() => {
            card.remove();
            if (bookmarkedItems.length === 0) {
              renderList([]);
            }
          }, 200);
        }
      });
      container.appendChild(card);
    });
  }
}
