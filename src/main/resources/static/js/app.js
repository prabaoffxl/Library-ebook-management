// Central API & Auth Manager

const API_BASE = '/api';

function getToken() {
    return localStorage.getItem('token');
}

function getUserRole() {
    return localStorage.getItem('role');
}

function getUserName() {
    return localStorage.getItem('name');
}

function checkAuth(requiredRoles = []) {
    const token = getToken();
    const role = getUserRole();

    if (!token) {
        window.location.href = '/login.html';
        return false;
    }

    if (requiredRoles.length > 0 && !requiredRoles.includes(role)) {
        redirectUserByRole(role);
        return false;
    }

    return true;
}

function redirectUserByRole(role) {
    if (role === 'ADMIN') {
        window.location.href = '/admin-dashboard.html';
    } else if (role === 'LIBRARIAN') {
        window.location.href = '/librarian-dashboard.html';
    } else {
        window.location.href = '/user-dashboard.html';
    }
}

function logout() {
    localStorage.clear();
    window.location.href = '/login.html';
}

async function apiFetch(endpoint, options = {}) {
    const token = getToken();
    const headers = options.headers || {};

    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    if (!(options.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    options.headers = headers;

    try {
        const response = await fetch(API_BASE + endpoint, options);

        if (response.status === 401) {
            localStorage.clear();
            window.location.href = '/login.html';
            throw new Error('Unauthorized session. Please login again.');
        }

        if (response.status === 403) {
            throw new Error('Access denied. You do not have permission.');
        }

        const data = await response.json();
        if (!response.ok || !data.success) {
            throw new Error(data.message || 'API Request Failed');
        }

        return data;
    } catch (err) {
        console.error('API Error:', err);
        throw err;
    }
}

function fetchWithAuth(url, method = 'GET', body = null) {
    const token = getToken();
    const headers = {};
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    const options = { method, headers };
    if (body) {
        headers['Content-Type'] = 'application/json';
        options.body = typeof body === 'string' ? body : JSON.stringify(body);
    }
    return fetch(url, options);
}

function fetchWithAuthMultipart(url, method = 'POST', formData) {
    const token = getToken();
    const headers = {};
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return fetch(url, {
        method: method,
        headers: headers,
        body: formData
    });
}

async function updateNotificationBadge() {
    const token = getToken();
    if (!token) return;
    try {
        const res = await apiFetch('/notifications/unread-count');
        const count = res.data ? res.data.unreadCount : 0;
        const badges = document.querySelectorAll('.notif-badge-counter');
        badges.forEach(b => {
            b.textContent = count;
            b.style.display = count > 0 ? 'inline-block' : 'none';
        });
    } catch (err) {
        console.warn('Failed to fetch unread notification count:', err);
    }
}

function initApp() {
    const token = getToken();
    if (!token) {
        window.location.href = '/login.html';
        return;
    }
    const userBadge = document.getElementById('userBadge');
    if (userBadge) {
        const role = getUserRole() || 'USER';
        const name = getUserName() || 'User';
        userBadge.innerText = `${name} (${role})`;
    }
    updateNotificationBadge();
}

function formatBytes(bytes) {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

function showAlert(elementId, message, type = 'danger') {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.className = `alert alert-${type}`;
    el.innerText = message;
    el.style.display = 'block';
    setTimeout(() => {
        el.style.display = 'none';
    }, 5000);
}
