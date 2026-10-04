// login.js - All API calls use explicit endpoints via window.api

// ================================
// LOAD ALL LOGINS INTO TABLE
// ================================
async function loadLogins() {
    let logins;
    try {
        const response = await window.api.get('/logins');
        logins = response.data;
    } catch (error) {
        console.error('GET /logins failed:', error.message);
        return;
    }

    const tbody = document.getElementById('loginsTable');

    if (logins.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-row">
                    No logins saved yet
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = '';

    logins.forEach(login => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${login.companyName || 'N/A'}</td>
            <td>
                <a href="${login.portalUrl}"
                   onclick="openUrl(
                       '${login.portalUrl}');
                       return false;"
                   style="color: var(--blue);">
                    ${shortenUrl(login.portalUrl)}
                </a>
            </td>
            <td>${login.email || 'N/A'}</td>
            <td>
                <span style="color: ${
                    login.isWorkday
                        ? 'var(--green)'
                        : 'var(--text-muted)'
                };">
                    ${login.isWorkday ? '✅ Yes' : '❌ No'}
                </span>
            </td>
            <td>
                <div style="display:flex; gap:5px;">
                    <button class="btn btn-blue btn-small"
                        onclick="editLogin(
                            '${login.portalUrl}')">
                        ✏️ Edit
                    </button>
                    <button class="btn btn-red btn-small"
                        onclick="deleteLogin(
                            '${login.portalUrl}')">
                        🗑️ Delete
                    </button>
                </div>
            </td>`;
        tbody.appendChild(tr);
    });
}


// ================================
// SAVE LOGIN
// ================================
async function saveLogin() {
    const companyName = document.getElementById(
        'loginCompany').value.trim();
    const portalUrl = document.getElementById(
        'loginUrl').value.trim();
    const email = document.getElementById(
        'loginEmail').value.trim();
    const password = document.getElementById(
        'loginPassword').value.trim();
    const isWorkday = document.getElementById(
        'isWorkday').checked;

    // Validation
    if (!companyName) {
        showToast('❌ Company name is required!', 'error');
        return;
    }
    if (!portalUrl) {
        showToast('❌ Portal URL is required!', 'error');
        return;
    }
    if (!email) {
        showToast('❌ Email is required!', 'error');
        return;
    }
    if (!password) {
        showToast('❌ Password is required!', 'error');
        return;
    }

    // Build login object
    const login = {
        companyName: companyName,
        portalUrl:   portalUrl,
        email:       email,
        password:    password,
        isWorkday:   isWorkday
    };

    try {
        await window.api.post('/logins', login);
        showToast('✅ Login saved!', 'success');
        clearLoginForm();
        loadLogins();
    } catch (error) {
        console.error('POST /logins failed:', error.message);
        showToast('❌ Failed to save login!', 'error');
    }
}


// ================================
// EDIT LOGIN
// ================================
async function editLogin(portalUrl) {
    let logins;
    try {
        const response = await window.api.get('/logins');
        logins = response.data;
    } catch (error) {
        console.error('GET /logins failed:', error.message);
        return;
    }

    // Find login by URL
    const login = logins.find(l => l.portalUrl === portalUrl);
    if (!login) return;

    // Fill form with login data
    document.getElementById('loginCompany')
        .value = login.companyName || '';
    document.getElementById('loginUrl')
        .value = login.portalUrl   || '';
    document.getElementById('loginEmail')
        .value = login.email       || '';
    document.getElementById('loginPassword')
        .value = login.password    || '';
    document.getElementById('isWorkday')
        .checked = login.isWorkday || false;

    // Scroll to form
    document.getElementById('loginCompany')
        .scrollIntoView({ behavior: 'smooth' });
    document.getElementById('loginCompany').focus();

    showToast('✏️ Edit the form and save!', 'info');
}


// ================================
// DELETE LOGIN
// ================================
async function deleteLogin(portalUrl) {
    if (!window.confirm(
        `Delete login for "${portalUrl}"?`)) {
        return;
    }

    // Encode URL for safe API call
    const encoded = encodeURIComponent(portalUrl);

    try {
        await window.api.delete('/logins/' + encoded);
        showToast('✅ Login deleted!', 'success');
        loadLogins();
    } catch (error) {
        console.error('DELETE /logins/:portalUrl failed:',
            error.message);
        showToast('❌ Failed to delete login!', 'error');
    }
}


// ================================
// CLEAR LOGIN FORM
// ================================
function clearLoginForm() {
    document.getElementById('loginCompany').value  = '';
    document.getElementById('loginUrl').value      = '';
    document.getElementById('loginEmail').value    = '';
    document.getElementById('loginPassword').value = '';
    document.getElementById('isWorkday').checked   = false;
}


// ================================
// OPEN URL IN BROWSER
// ================================
function openUrl(url) {
    const { shell } = require('electron');
    shell.openExternal(url);
}


// ================================
// SHORTEN URL FOR DISPLAY
// ================================
function shortenUrl(url) {
    if (!url) return 'N/A';
    try {
        const parsed = new URL(
            url.startsWith('http')
                ? url
                : 'https://' + url
        );
        return parsed.hostname;
    } catch {
        return url.length > 30
            ? url.substring(0, 30) + '...'
            : url;
    }
}