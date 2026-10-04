// formfiller.js - All API calls use explicit endpoints via window.api

let isFillingForm = false;
let pollInterval  = null;

// ================================
// START FILLING FORM
// ================================
async function startFilling() {
    const profileName = document.getElementById(
        'fillProfileDropdown').value;
    const formUrl = document.getElementById(
        'formUrl').value.trim();

    // Validation
    if (!profileName) {
        showToast('❌ Select a profile first!', 'error');
        return;
    }
    if (!formUrl) {
        showToast('❌ Enter a job form URL!', 'error');
        return;
    }
    if (!formUrl.startsWith('http')) {
        showToast(
            '❌ URL must start with http/https!',
            'error'
        );
        return;
    }

    // Get settings from UI
    const autoSubmit = document.getElementById(
        'autoSubmit').checked;
    const headless = document.getElementById(
        'headlessMode').checked;
    const browser = document.getElementById(
        'browser').value;

    // Build request
    const request = {
        profileName: profileName,
        formUrl:     formUrl,
        autoSubmit:  autoSubmit,
        headless:    headless,
        browser:     browser
    };

    // Update UI
    setFillingState(true);
    clearLog();
    addLog('🚀 Starting form filler...');
    addLog('📋 Profile  : ' + profileName);
    addLog('🔗 URL      : ' + formUrl);
    addLog('🌐 Browser  : ' + browser);
    addLog('📤 Auto Submit: ' + autoSubmit);
    addLog('👻 Headless : ' + headless);
    addLog('─────────────────────────────');

    // Call backend: POST /fill/start
    try {
        const response = await window.api.post(
            '/fill/start', request);
        handleFillResult(response.data);
    } catch (error) {
        console.error('POST /fill/start failed:', error.message);
        addLog('❌ Failed to start form filler!');
        setFillingState(false);
    }
}


// ================================
// HANDLE FILL RESULT
// ================================
function handleFillResult(result) {
    if (result.status === 'DUPLICATE') {
        addLog('⚠️  DUPLICATE APPLICATION DETECTED!');
        addLog('⚠️  You already applied to this job!');
        if (result.previousApplication) {
            addLog('📅 Applied on: ' +
                formatDate(
                    result.previousApplication.appliedAt
                ));
        }
        showToast('⚠️ Duplicate application!', 'warning');
        setFillingState(false);
        return;
    }

    if (result.status === 'STARTED') {
        addLog('✅ Form filler started!');
        addLog('🤖 Selenium is filling the form...');
        addLog('👀 Watch the browser window!');
        addLog('─────────────────────────────');
        showToast('🚀 Form filling started!', 'info');

        // Poll for completion
        startPolling(result.formUrl);
        return;
    }

    if (result.status === 'SUCCESS') {
        addLog('✅ Form filled successfully!');
        addLog('🎉 Application submitted!');
        showToast('✅ Form filled!', 'success');
        setFillingState(false);
        return;
    }

    if (result.status === 'ERROR') {
        addLog('❌ Error: ' + (result.message ||
            'Unknown error'));
        showToast('❌ Form filling failed!', 'error');
        setFillingState(false);
        return;
    }
}


// ================================
// POLL FOR COMPLETION
// ================================
function startPolling(formUrl) {
    let pollCount = 0;
    const maxPolls = 60; // 5 minutes max

    pollInterval = setInterval(async () => {
        pollCount++;

        // Check history: GET /fill/history
        let history;
        try {
            const response = await window.api.get(
                '/fill/history');
            history = response.data;
        } catch (error) {
            console.error('GET /fill/history failed:',
                error.message);
            return;
        }

        // Find latest entry for this URL
        const latest = history.find(h =>
            h.formUrl === formUrl
        );

        if (latest) {
            if (latest.status === 'SUCCESS') {
                clearInterval(pollInterval);
                addLog('✅ Form filled successfully!');
                addLog('🎉 Application saved to history!');
                showToast(
                    '✅ Application submitted!',
                    'success'
                );
                setFillingState(false);
                return;
            }

            if (latest.status === 'FAILED') {
                clearInterval(pollInterval);
                addLog('❌ Form filling failed!');
                addLog('💡 Check screenshots for details');
                showToast(
                    '❌ Form filling failed!',
                    'error'
                );
                setFillingState(false);
                return;
            }
        }

        // Timeout check
        if (pollCount >= maxPolls) {
            clearInterval(pollInterval);
            addLog('⚠️  Timeout! Check browser window.');
            showToast('⚠️ Timeout!', 'warning');
            setFillingState(false);
        }

        // Progress dots
        if (pollCount % 3 === 0) {
            addLog('⏳ Still filling form...');
        }

    }, 5000); // Poll every 5 seconds
}


// ================================
// SET FILLING STATE
// ================================
function setFillingState(filling) {
    isFillingForm = filling;
    const btn = document.getElementById('startBtn');

    if (filling) {
        btn.disabled    = true;
        btn.textContent = '⏳ FILLING FORM...';
        btn.style.background = '#6b7280';
    } else {
        btn.disabled    = false;
        btn.textContent = '🚀 START FILLING FORM';
        btn.style.background = '';
    }
}


// ================================
// STOP FILLING
// ================================
function stopFilling() {
    if (pollInterval) {
        clearInterval(pollInterval);
    }
    setFillingState(false);
    addLog('⛔ Stopped by user');
    showToast('⛔ Stopped!', 'warning');
}


// ================================
// LOAD APPLICATION HISTORY
// ================================
async function loadHistory() {
    let history;
    try {
        const response = await window.api.get('/fill/history');
        history = response.data;
    } catch (error) {
        console.error('GET /fill/history failed:', error.message);
        return;
    }

    const container = document.getElementById(
        'historyContainer');
    if (!container) return;

    if (history.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                No applications yet
            </div>`;
        return;
    }

    // Build table
    let html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Company/URL</th>
                    <th>Profile</th>
                    <th>Status</th>
                    <th>Date</th>
                </tr>
            </thead>
            <tbody>`;

    history.forEach(item => {
        const statusColor =
            item.status === 'SUCCESS' ? 'var(--green)' :
            item.status === 'FAILED'  ? 'var(--red)'   :
                                        'var(--yellow)';

        const shortUrl = item.formUrl
            ? item.formUrl.substring(0, 40) + '...'
            : 'N/A';

        html += `
            <tr>
                <td title="${item.formUrl}">
                    ${shortUrl}
                </td>
                <td>${item.profileName || 'N/A'}</td>
                <td style="color:${statusColor};
                           font-weight:600;">
                    ${item.status}
                </td>
                <td>${formatDate(item.appliedAt)}</td>
            </tr>`;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}