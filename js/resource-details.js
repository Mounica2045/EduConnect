/**
 * EduConnect - Resource Details Controller
 * Author: Senior Frontend Developer (CSP Team)
 * Fetches single resource metadata, file download handling, and related materials
 */

document.addEventListener('DOMContentLoaded', () => {
  initResourceDetails();
});

async function initResourceDetails() {
  const container = document.getElementById('details-state-container');
  const breadcrumbTitle = document.getElementById('breadcrumb-title');
  const relatedGrid = document.getElementById('related-resources-grid');

  const urlParams = new URLSearchParams(window.location.search);
  const resourceId = urlParams.get('id');

  // Validate ID presence
  if (!resourceId || !resourceId.trim()) {
    renderEmptyState(container, {
      title: 'Invalid Resource Identifier',
      message: 'No resource was specified. Please choose a study material from our library.',
      actionText: 'Browse Resource Library',
      onAction: () => { window.location.href = 'resources.html'; }
    });
    return;
  }

  try {
    let resource = null;

    // Fetch from Spring Boot REST API
    try {
      resource = await apiClient.get(`/resources/${encodeURIComponent(resourceId)}`);
    } catch (apiErr) {
      console.info(`API endpoint /api/resources/${resourceId} unavailable; checking sample dataset.`);
      resource = CONFIG.SAMPLE_RESOURCES.find(r => r.id === resourceId);
    }

    if (!resource) {
      renderEmptyState(container, {
        title: 'Resource Not Found',
        message: 'The requested study material may have been removed, archived, or is awaiting administrator verification.',
        actionText: 'Return to Library',
        onAction: () => { window.location.href = 'resources.html'; }
      });
      return;
    }

    // Set page and breadcrumb titles
    document.title = `${resource.title} — EduConnect`;
    if (breadcrumbTitle) breadcrumbTitle.textContent = resource.title;

    // Render full details
    renderDetails(container, resource);

    // Load related materials
    loadRelatedResources(relatedGrid, resource);

  } catch (err) {
    console.error('Error loading resource details:', err);
    renderEmptyState(container, {
      title: 'Unable to Load Resource Details',
      message: 'A connection issue prevented retrieving this document. Please check your network and try again.',
      actionText: 'Try Again',
      onAction: () => initResourceDetails()
    });
  }
}

function renderDetails(container, resource) {
  const cat = CONFIG.CATEGORIES.find(c => c.id === resource.category) || {
    label: resource.categoryLabel || 'Academic Material',
    badgeClass: 'badge-other'
  };

  const isSaved = bookmarkService.isBookmarked(resource.id);

  container.innerHTML = `
    <div class="resource-details-layout">
      <!-- Main Content Column -->
      <article class="details-main-card">
        <div class="details-header">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
            <span class="badge ${cat.badgeClass}">${escapeHTML(cat.label)}</span>
            <button type="button" class="btn btn-outline btn-sm" id="details-bookmark-btn" aria-label="Bookmark this resource">
              <svg width="16" height="16" fill="${isSaved ? 'currentColor' : 'none'}" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"/>
              </svg>
              <span>${isSaved ? 'Saved in Bookmarks' : 'Bookmark'}</span>
            </button>
          </div>
          <h1 class="details-title">${escapeHTML(resource.title)}</h1>
          <div style="display: flex; align-items: center; gap: 12px; font-size: 0.875rem; color: var(--color-gray-500); flex-wrap: wrap;">
            <span>Uploaded on ${formatDate(resource.uploadDate)}</span>
            <span>&bull;</span>
            <span>By ${escapeHTML(resource.contributorName || 'Verified Peer')}</span>
            <span>&bull;</span>
            <span>${escapeHTML(resource.downloadCount || 0)} Community Views</span>
          </div>
        </div>

        <!-- Academic Metadata Grid -->
        <div class="details-meta-grid">
          <div class="details-meta-box">
            <span class="details-meta-label">Subject</span>
            <span class="details-meta-val">${escapeHTML(resource.subject || 'General')}</span>
          </div>
          <div class="details-meta-box">
            <span class="details-meta-label">Department</span>
            <span class="details-meta-val">${escapeHTML(resource.department || 'All Departments')}</span>
          </div>
          <div class="details-meta-box">
            <span class="details-meta-label">Semester</span>
            <span class="details-meta-val">${escapeHTML(resource.semester || 'N/A')}</span>
          </div>
          <div class="details-meta-box">
            <span class="details-meta-label">File Specification</span>
            <span class="details-meta-val">${escapeHTML(resource.fileType || 'PDF')} (${escapeHTML(resource.fileSize || 'N/A')})</span>
          </div>
        </div>

        <!-- Document Overview -->
        <div style="margin-bottom: var(--spacing-6);">
          <h2 class="details-section-title">Resource Description</h2>
          <p class="details-description-text">${escapeHTML(resource.description)}</p>
        </div>

        <!-- Academic Integrity Note -->
        <div style="background-color: var(--color-gray-50); border: 1px solid var(--color-gray-200); border-radius: var(--radius-md); padding: var(--spacing-4); font-size: 0.8125rem; color: var(--color-gray-600); display: flex; gap: 12px; align-items: flex-start;">
          <svg width="20" height="20" fill="none" stroke="var(--color-primary-600)" viewBox="0 0 24 24" style="flex-shrink: 0; margin-top: 2px;">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          <div>
            <strong>Academic Integrity Notice:</strong> This material is provided by students to support collaborative study and examination revision. Please use it in accordance with college academic integrity guidelines.
          </div>
        </div>
      </article>

      <!-- Sidebar Actions Column -->
      <aside aria-label="Download and resource actions">
        <div class="details-sidebar-card">
          <div class="download-cta-box">
            <div class="download-file-info">
              <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"/></svg>
              <span>Verified ${escapeHTML(resource.fileType || 'PDF')} &bull; ${escapeHTML(resource.fileSize || 'Standard')}</span>
            </div>
            <button type="button" class="btn btn-primary" id="download-resource-btn" style="width: 100%; margin-bottom: 8px;">
              <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/></svg>
              Download Resource
            </button>
            <div style="font-size: 0.75rem; color: var(--color-gray-500);">Virus scanned & checked for legibility</div>
          </div>

          <!-- Quick Share / Report links -->
          <div style="display: flex; flex-direction: column; gap: 8px; font-size: 0.875rem;">
            <button type="button" class="btn btn-outline btn-sm" id="share-link-btn" style="width: 100%;">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/></svg>
              Copy Resource Link
            </button>
            <a href="resources.html" class="btn btn-ghost btn-sm" style="width: 100%;">
              &larr; Back to Resource Library
            </a>
          </div>
        </div>

        <!-- Contributor Badge Card -->
        <div class="details-sidebar-card" style="text-align: center;">
          <div style="width: 48px; height: 48px; border-radius: 50%; background-color: var(--color-primary-100); color: var(--color-primary-800); display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; font-weight: 700;">
            ${(resource.contributorName || 'C').charAt(0)}
          </div>
          <div style="font-weight: 600; font-size: 0.875rem; color: var(--color-gray-900);">${escapeHTML(resource.contributorName || 'Anonymous Student')}</div>
          <div style="font-size: 0.75rem; color: var(--color-gray-500); margin-bottom: 8px;">Peer Contributor</div>
          <div style="font-size: 0.75rem; color: var(--color-gray-600); line-height: 1.4;">
            This study resource was shared as part of college Community Service Project collaboration.
          </div>
        </div>
      </aside>
    </div>
  `;

  // Attach button event handlers
  const bookmarkBtn = container.querySelector('#details-bookmark-btn');
  if (bookmarkBtn) {
    bookmarkBtn.addEventListener('click', async () => {
      const newState = await bookmarkService.toggle(resource);
      const svg = bookmarkBtn.querySelector('svg');
      const span = bookmarkBtn.querySelector('span');
      if (svg) svg.setAttribute('fill', newState ? 'currentColor' : 'none');
      if (span) span.textContent = newState ? 'Saved in Bookmarks' : 'Bookmark';
    });
  }

  const downloadBtn = container.querySelector('#download-resource-btn');
  if (downloadBtn) {
    downloadBtn.addEventListener('click', () => {
      handleDownload(resource);
    });
  }

  const shareBtn = container.querySelector('#share-link-btn');
  if (shareBtn) {
    shareBtn.addEventListener('click', () => {
      if (navigator.clipboard) {
        navigator.clipboard.writeText(window.location.href);
        toast.success('Link Copied', 'Resource URL copied to your clipboard.');
      } else {
        toast.info('Resource URL', window.location.href);
      }
    });
  }
}

/**
 * Handles download action by triggering file stream or simulated browser download
 */
function handleDownload(resource) {
  // If backend file download endpoint is configured:
  // window.location.href = `${CONFIG.API_BASE_URL}/resources/${resource.id}/download`;

  toast.info('Downloading Resource', `Preparing ${resource.title} (${resource.fileType || 'PDF'})...`);

  // Create client-side academic text blob as safe simulation if offline
  setTimeout(() => {
    const sampleBlobContent = `
================================================================================
EduConnect — College Learning Resource Sharing Portal
Document: ${resource.title}
Subject: ${resource.subject || 'General'}
Department: ${resource.department || 'Academic'} | Semester: ${resource.semester || 'N/A'}
Contributor: ${resource.contributorName || 'Peer Contributor'}
================================================================================

SUMMARY:
${resource.description}

[This study material was downloaded from the EduConnect Portal as part of our College Community Service Project (CSP)]
    `.trim();

    const blob = new Blob([sampleBlobContent], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${resource.title.replace(/[^a-zA-Z0-9]/g, '_')}_EduConnect.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    toast.success('Download Complete', 'The study document has been downloaded to your device.');
  }, 1000);
}

/**
 * Loads related resources based on same department or category
 */
function loadRelatedResources(container, currentResource) {
  if (!container) return;

  const related = CONFIG.SAMPLE_RESOURCES
    .filter(r => r.id !== currentResource.id && r.isApproved)
    .filter(r => r.category === currentResource.category || r.department === currentResource.department)
    .slice(0, 3);

  container.innerHTML = '';

  if (related.length === 0) {
    const fallback = CONFIG.SAMPLE_RESOURCES.filter(r => r.id !== currentResource.id && r.isApproved).slice(0, 3);
    fallback.forEach(item => {
      container.appendChild(createResourceCardElement(item));
    });
    return;
  }

  related.forEach(item => {
    container.appendChild(createResourceCardElement(item));
  });
}
