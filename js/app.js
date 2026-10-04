/**
 * 1. TOAST NOTIFICATION LOGIC
 * This must be defined before it is called!
 */
window.showToast = function(message, type = 'success') {
    const toast = document.getElementById('toast');
    if (!toast) return;

    toast.textContent = message;
    toast.className = `toast show ${type}`;

    // Auto-hide after 3 seconds
    setTimeout(() => {
        toast.className = toast.className.replace('show', '');
    }, 3000);
};

/**
 * 2. TAB NAVIGATION
 */
window.showTab = function(tabId) {
    console.log("Switching to tab:", tabId);
    
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));
    
    if (event) {
        event.currentTarget.classList.add('active');
    }

    const targetPane = document.getElementById('tab-' + tabId);
    if (targetPane) {
        targetPane.classList.add('active');
    } else {
        console.error("Could not find tab pane with id:", 'tab-' + tabId);
    }
};

/**
 * 3. FILE BROWSING
 */
window.browseFile = async function(inputId) {
    try {
        const { dialog } = require('@electron/remote');

        const result = await dialog.showOpenDialog({
            title: 'Select File',
            properties: ['openFile'],
            filters: getFileFilters(inputId)
        });

        if (!result.canceled && result.filePaths.length > 0) {
            document.getElementById(inputId).value = result.filePaths[0];
            showToast('✅ File selected!', 'success');
        }
    } catch (error) {
        console.error('Browse error:', error);
        showToast('❌ Error: ' + error.message, 'error');
    }
};

/**
 * 4. FOLDER BROWSING
 */
window.browseFolder = async function(inputId) {
    try {
        const { dialog } = require('@electron/remote');

        const result = await dialog.showOpenDialog({
            title: 'Select Folder',
            properties: ['openDirectory']
        });

        if (!result.canceled && result.filePaths.length > 0) {
            document.getElementById(inputId).value = result.filePaths[0];
            showToast('✅ Folder selected!', 'success');
        }
    } catch (error) {
        console.error('Browse error:', error);
        showToast('❌ Error: ' + error.message, 'error');
    }
};

/**
 * 5. FILTERS HELPER
 */
function getFileFilters(inputId) {
    if (inputId === 'photoPath') {
        return [{ name: 'Images', extensions: ['jpg', 'jpeg', 'png'] }];
    }
    if (inputId === 'resumePath' || inputId === 'coverLetterPath') {
        return [
            { name: 'PDF Files', extensions: ['pdf'] },
            { name: 'All Files', extensions: ['*'] }
        ];
    }
    return [{ name: 'All Files', extensions: ['*'] }];
}