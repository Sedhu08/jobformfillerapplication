// profile.js - All API calls use explicit endpoints via window.api

// ================================
// LOAD ALL PROFILES INTO DROPDOWN
// ================================
async function loadProfiles() {
    let profiles;
    try {
        const response = await window.api.get('/profiles');
        profiles = response.data;
    } catch (error) {
        console.error('GET /profiles failed:', error.message);
        return;
    }

    const dropdown = document.getElementById('profileDropdown');
    dropdown.innerHTML = '';

    if (profiles.length === 0) {
        dropdown.innerHTML =
            '<option value="">No profiles yet</option>';
        return;
    }

    profiles.forEach(profile => {
        const option = document.createElement('option');
        option.value = profile.profileName;
        option.textContent = profile.profileName +
            (profile.isDefault ? ' ⭐ (Default)' : '');
        dropdown.appendChild(option);
    });

    // Load first profile data
    loadProfile();
}


// ================================
// LOAD PROFILE DATA INTO FORM
// ================================
async function loadProfile() {
    const dropdown = document.getElementById('profileDropdown');
    const profileName = dropdown.value;
    if (!profileName) return;

    let profile;
    try {
        const response = await window.api.get(
            '/profiles/' + profileName);
        profile = response.data;
    } catch (error) {
        console.error('GET /profiles/:name failed:', error.message);
        return;
    }

    // Fill form fields
    document.getElementById('profileName')
        .value = profile.profileName || '';
    document.getElementById('fullName')
        .value = profile.fullName || '';
    document.getElementById('email')
        .value = profile.email || '';
    document.getElementById('phone')
        .value = profile.phone || '';
    document.getElementById('address')
        .value = profile.address || '';
    document.getElementById('linkedinUrl')
        .value = profile.linkedinUrl || '';
    document.getElementById('experience')
        .value = profile.experience || '';
    document.getElementById('skills')
        .value = profile.skills || '';
    document.getElementById('education')
        .value = profile.education || '';
    document.getElementById('resumePath')
        .value = profile.resumePath || '';
    document.getElementById('coverLetterPath')
        .value = profile.coverLetterPath || '';
    document.getElementById('photoPath')
        .value = profile.photoPath || '';
    document.getElementById('isDefault')
        .checked = profile.isDefault || false;
}


// ================================
// SAVE PROFILE
// ================================
async function saveProfile() {
    const profileName = document.getElementById(
        'profileName').value.trim();

    if (!profileName) {
        showToast('❌ Profile name is required!', 'error');
        return;
    }

    const fullName = document.getElementById(
        'fullName').value.trim();
    if (!fullName) {
        showToast('❌ Full name is required!', 'error');
        return;
    }

    const email = document.getElementById(
        'email').value.trim();
    if (!email) {
        showToast('❌ Email is required!', 'error');
        return;
    }

    // Build profile object
    const profile = {
        profileName: profileName,
        fullName: fullName,
        email: email,
        phone: document.getElementById(
            'phone').value.trim(),
        address: document.getElementById(
            'address').value.trim(),
        linkedinUrl: document.getElementById(
            'linkedinUrl').value.trim(),
        experience: document.getElementById(
            'experience').value.trim(),
        skills: document.getElementById(
            'skills').value.trim(),
        education: document.getElementById(
            'education').value.trim(),
        resumePath: document.getElementById(
            'resumePath').value.trim(),
        coverLetterPath: document.getElementById(
            'coverLetterPath').value.trim(),
        photoPath: document.getElementById(
            'photoPath').value.trim(),
        isDefault: document.getElementById(
            'isDefault').checked
    };

    try {
        await window.api.post('/profiles', profile);
        showToast('✅ Profile saved!', 'success');
        loadProfiles();
        loadProfilesForFillForm();
    } catch (error) {
        console.error('POST /profiles failed:', error.message);
        showToast('❌ Failed to save profile!', 'error');
    }
}


// ================================
// NEW PROFILE - CLEAR FORM
// ================================
function newProfile() {
    document.getElementById('profileName').value = '';
    document.getElementById('fullName').value = '';
    document.getElementById('email').value = '';
    document.getElementById('phone').value = '';
    document.getElementById('address').value = '';
    document.getElementById('linkedinUrl').value = '';
    document.getElementById('experience').value = '';
    document.getElementById('skills').value = '';
    document.getElementById('education').value = '';
    document.getElementById('resumePath').value = '';
    document.getElementById('coverLetterPath').value = '';
    document.getElementById('photoPath').value = '';
    document.getElementById('isDefault').checked = false;

    document.getElementById('profileName').focus();
    showToast('✅ New profile form ready!', 'info');
}


// ================================
// DUPLICATE PROFILE
// ================================
async function duplicateProfile() {
    const dropdown = document.getElementById('profileDropdown');
    const profileName = dropdown.value;

    if (!profileName) {
        showToast('❌ Select a profile first!', 'error');
        return;
    }

    try {
        await window.api.post(
            '/profiles/' + profileName + '/duplicate');
        showToast('✅ Profile duplicated!', 'success');
        loadProfiles();
    } catch (error) {
        console.error('POST /profiles/:name/duplicate failed:',
            error.message);
        showToast('❌ Failed to duplicate profile!', 'error');
    }
}


// ================================
// DELETE PROFILE
// ================================
async function deleteProfile() {
    const dropdown = document.getElementById('profileDropdown');
    const profileName = dropdown.value;

    if (!profileName) {
        showToast('❌ Select a profile first!', 'error');
        return;
    }

    if (!window.confirm(
        `Delete profile "${profileName}"?`)) {
        return;
    }

    const encoded = encodeURIComponent(profileName);

    try {
        await window.api.delete('/profiles/' + encoded);
        showToast('✅ Profile deleted!', 'success');
        newProfile();
        loadProfiles();
        loadProfilesForFillForm();
    } catch (error) {
        console.error('DELETE profiles/:name failed:',
            error.message);
        showToast('❌ Failed to delete profile!', 'error');
    }
}


// ================================
// LOAD PROFILES FOR FILL FORM TAB
// ================================
async function loadProfilesForFillForm() {
    let profiles;
    try {
        const response = await window.api.get('/profiles');
        profiles = response.data;
    } catch (error) {
        console.error('GET /profiles failed:', error.message);
        return;
    }

    const dropdown = document.getElementById(
        'fillProfileDropdown');
    dropdown.innerHTML = '';

    if (profiles.length === 0) {
        dropdown.innerHTML =
            '<option value="">No profiles yet</option>';
        return;
    }

    profiles.forEach(profile => {
        const option = document.createElement('option');
        option.value = profile.profileName;
        option.textContent = profile.profileName +
            (profile.isDefault ? ' ⭐ (Default)' : '');

        // Auto select default profile
        if (profile.isDefault) {
            option.selected = true;
        }
        dropdown.appendChild(option);
    });
}