// settings.js - All API calls use explicit endpoints via window.api

// ================================
// SAVE SETTINGS
// ================================
async function saveSettings() {
    // Telegram
    const telegramEnabled = document.getElementById(
        'telegramEnabled').checked;
    const telegramToken   = document.getElementById(
        'telegramToken').value.trim();
    const telegramChatId  = document.getElementById(
        'telegramChatId').value.trim();

    // Validate telegram if enabled
    if (telegramEnabled) {
        if (!telegramToken) {
            showToast(
                '❌ Telegram Bot Token is required!',
                'error'
            );
            return;
        }
        if (!telegramChatId) {
            showToast(
                '❌ Telegram Chat ID is required!',
                'error'
            );
            return;
        }
    }

    // Browser
    const browser = document.getElementById(
        'defaultBrowser').value;
    const formFillDelay = parseInt(
        document.getElementById(
            'formFillDelay').value
    ) || 1;
    const headlessMode = document.getElementById(
        'headlessModeSettings').checked;
    const autoSubmit = document.getElementById(
        'autoSubmitSettings').checked;

    // File paths
    const screenshotPath = document.getElementById(
        'screenshotPath').value.trim();
    const errorLogPath = document.getElementById(
        'errorLogPath').value.trim();

    // Validate delay
    if (formFillDelay < 1 || formFillDelay > 10) {
        showToast(
            '❌ Delay must be between 1-10 seconds!',
            'error'
        );
        return;
    }

    // Build settings object
    const settings = {
        telegramEnabled:  telegramEnabled,
        telegramBotToken: telegramToken,
        telegramChatId:   telegramChatId,
        browser:          browser,
        formFillDelay:    formFillDelay,
        headlessMode:     headlessMode,
        autoSubmit:       autoSubmit,
        screenshotPath:   screenshotPath,
        errorLogPath:     errorLogPath
    };

    try {
        await window.api.post('/settings', settings);
        showToast('✅ Settings saved!', 'success');
    } catch (error) {
        console.error('POST /settings failed:', error.message);
        showToast('❌ Failed to save settings!', 'error');
    }
}


// ================================
// LOAD SETTINGS INTO FORM
// ================================
async function loadSettings() {
    let settings;
    try {
        const response = await window.api.get('/settings');
        settings = response.data;
    } catch (error) {
        console.error('GET /settings failed:', error.message);
        return;
    }

    document.getElementById('telegramEnabled')
        .checked = settings.telegramEnabled || false;
    document.getElementById('telegramToken')
        .value = settings.telegramBotToken || '';
    document.getElementById('telegramChatId')
        .value = settings.telegramChatId || '';
    document.getElementById('defaultBrowser')
        .value = settings.browser || 'CHROME';
    document.getElementById('formFillDelay')
        .value = settings.formFillDelay || 1;
    document.getElementById('headlessModeSettings')
        .checked = settings.headlessMode || false;
    document.getElementById('autoSubmitSettings')
        .checked = settings.autoSubmit || false;
    document.getElementById('screenshotPath')
        .value = settings.screenshotPath || '';
    document.getElementById('errorLogPath')
        .value = settings.errorLogPath || '';
}


// ================================
// TEST TELEGRAM BOT
// ================================
async function testTelegram() {
    const token  = document.getElementById(
        'telegramToken').value.trim();
    const chatId = document.getElementById(
        'telegramChatId').value.trim();

    if (!token) {
        showToast('❌ Enter Bot Token first!', 'error');
        return;
    }
    if (!chatId) {
        showToast('❌ Enter Chat ID first!', 'error');
        return;
    }

    // Show loading
    const resultSpan = document.getElementById(
        'telegramTestResult');
    resultSpan.textContent = '⏳ Testing...';
    resultSpan.style.color = 'var(--yellow)';
    resultSpan.style.marginLeft = '10px';
    resultSpan.style.fontSize   = '13px';

    try {
        const response = await window.api.post(
            '/settings/telegram/test',
            { token: token, chatId: chatId }
        );
        const result = response.data;

        if (result && result.success) {
            resultSpan.textContent = '✅ Message sent!';
            resultSpan.style.color = 'var(--green)';
            showToast('✅ Telegram test successful!', 'success');
        } else {
            resultSpan.textContent = '❌ Failed!';
            resultSpan.style.color = 'var(--red)';
            showToast(
                '❌ Telegram test failed! Check token/chatId',
                'error'
            );
        }
    } catch (error) {
        console.error('POST /settings/telegram/test failed:',
            error.message);
        resultSpan.textContent = '❌ Error!';
        resultSpan.style.color = 'var(--red)';
        showToast('❌ Telegram test failed!', 'error');
    }

    // Clear result after 5 seconds
    setTimeout(() => {
        resultSpan.textContent = '';
    }, 5000);
}


// ================================
// RESET SETTINGS TO DEFAULT
// ================================
async function resetSettings() {
    if (!window.confirm('Reset all settings to default?')) {
        return;
    }

    const defaults = {
        telegramEnabled:  false,
        telegramBotToken: '',
        telegramChatId:   '',
        browser:          'CHROME',
        formFillDelay:    1,
        headlessMode:     false,
        autoSubmit:       false,
        screenshotPath:   'C:/jobfiller/screenshots',
        errorLogPath:     'C:/jobfiller/logs'
    };

    try {
        await window.api.post('/settings', defaults);
        loadSettings();
        showToast('✅ Settings reset to default!', 'success');
    } catch (error) {
        console.error('POST /settings (reset) failed:',
            error.message);
        showToast('❌ Failed to reset settings!', 'error');
    }
}