// screenshots.js - All API calls use explicit endpoints via window.api

// ================================
// LOAD ALL SCREENSHOTS
// ================================
async function loadScreenshots() {
    let screenshots;
    try {
        const response = await window.api.get('/screenshots');
        screenshots = response.data;
    } catch (error) {
        console.error('GET /screenshots failed:', error.message);
        screenshots = [];
    }

    const grid = document.getElementById('screenshotsGrid');

    if (!screenshots || screenshots.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                📸 No screenshots yet.<br>
                <small style="color:var(--text-muted);">
                    Screenshots are taken automatically
                    when a form is filled.
                </small>
            </div>`;
        return;
    }

    grid.innerHTML = '';

    screenshots.forEach(screenshot => {
        const item = document.createElement('div');
        item.className = 'screenshot-item';
        item.onclick   = () => openScreenshot(
            screenshot.path
        );

        // Format file name
        const fileName = screenshot.name ||
            screenshot.path.split('\\').pop()
                           .split('/').pop();

        // Format date
        const date = screenshot.createdAt
            ? formatDate(screenshot.createdAt)
            : '';

        // Status color
        const statusColor =
            screenshot.status === 'SUCCESS'
                ? 'var(--green)'
                : screenshot.status === 'ERROR'
                    ? 'var(--red)'
                    : 'var(--text-muted)';

        item.innerHTML = `
            <div class="screenshot-icon">📸</div>
            <div class="screenshot-name"
                 title="${fileName}">
                ${shortenFileName(fileName)}
            </div>
            <div style="
                font-size: 10px;
                color: ${statusColor};
                margin-top: 4px;
                font-weight: 600;">
                ${screenshot.status || ''}
            </div>
            <div style="
                font-size: 10px;
                color: var(--text-muted);
                margin-top: 2px;">
                ${date}
            </div>`;

        grid.appendChild(item);
    });
}


// ================================
// OPEN SCREENSHOT FILE
// ================================
function openScreenshot(filePath) {
    if (!filePath) return;
    const { shell } = require('electron');
    shell.openPath(filePath);
}


// ================================
// OPEN SCREENSHOTS FOLDER
// ================================
async function openScreenshotsFolder() {
    try {
        const response = await window.api.get(
            '/screenshots/open-folder');
        const result = response.data;

        if (result && result.path) {
            const { shell } = require('electron');
            shell.openPath(result.path);
            showToast(
                '📂 Opening screenshots folder...',
                'info'
            );
        } else {
            showToast(
                '❌ Screenshots folder not found!',
                'error'
            );
        }
    } catch (error) {
        console.error('GET /screenshots/open-folder failed:',
            error.message);
        showToast('❌ Could not open folder!', 'error');
    }
}


// ================================
// REFRESH SCREENSHOTS
// ================================
async function refreshScreenshots() {
    showToast('🔄 Refreshing...', 'info');
    await loadScreenshots();
    showToast('✅ Screenshots refreshed!', 'success');
}


// ================================
// DELETE SCREENSHOT
// ================================
async function deleteScreenshot(fileName) {
    if (!window.confirm(
        `Delete screenshot "${fileName}"?`)) {
        return;
    }

    const encoded = encodeURIComponent(fileName);

    try {
        await window.api.delete('/screenshots/' + encoded);
        showToast('✅ Screenshot deleted!', 'success');
        loadScreenshots();
    } catch (error) {
        console.error('DELETE /screenshots/:fileName failed:',
            error.message);
        showToast('❌ Failed to delete screenshot!', 'error');
    }
}


// ================================
// SHORTEN FILE NAME FOR DISPLAY
// ================================
function shortenFileName(fileName) {
    if (!fileName) return 'Unknown';
    if (fileName.length <= 20) return fileName;
    const ext  = fileName.split('.').pop();
    const name = fileName.substring(0, 15);
    return `${name}...${ext}`;
}