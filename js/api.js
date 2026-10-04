// api.js - Shared axios instance for all JS files
const axios = require('axios');

const BASE_URL = 'http://localhost:8081';

// Shared axios instance with base URL
window.api = axios.create({
    baseURL: BASE_URL,
    headers: { 'Content-Type': 'application/json' }
});