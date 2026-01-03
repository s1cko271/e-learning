/**
 * API Client cho Backend
 */
import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { API_BASE_URL, STORAGE_KEYS } from './constants';

// Tạo axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Thêm token vào header
apiClient.interceptors.request.use(
  (config) => {
    // Nếu là FormData, không set Content-Type (browser sẽ tự động set với boundary)
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }
    
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        // Debug logging for enrollment endpoints
        if (config.url?.includes('/enrollments/')) {
          console.log('Enrollment Request - Token attached:', {
            url: `${config.baseURL}${config.url}`,
            method: config.method?.toUpperCase(),
            hasToken: !!token,
            tokenLength: token.length,
          });
        }
      } else {
        // Log if no token is found for enrollment requests
        if (config.url?.includes('/enrollments/')) {
          console.warn('Enrollment Request - No token found:', {
            url: `${config.baseURL}${config.url}`,
            method: config.method?.toUpperCase(),
          });
        }
      }
    }
    
    // Log request details for login endpoint (for debugging)
    if (config.url?.includes('/auth/login')) {
      console.log('Login Request Details:', {
        url: `${config.baseURL}${config.url}`,
        method: config.method?.toUpperCase(),
        headers: {
          'Content-Type': config.headers['Content-Type'],
          'Authorization': config.headers.Authorization ? 'Bearer ***' : 'None',
        },
        data: config.data ? { ...JSON.parse(JSON.stringify(config.data)), password: '***' } : null,
      });
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Xử lý lỗi
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    // Skip logging for cart API 400/403 errors when user is not authenticated
    // These are expected during initial page load
    const isCartApi = error.config?.url?.includes('/cart');
    const isContentApi = error.config?.url?.includes('/content/courses');
    const isNotificationApi = error.config?.url?.includes('/notifications');
    const isStudentApi = error.config?.url?.includes('/student/') || error.config?.url?.includes('/my-courses');
    const is400Error = error.response?.status === 400;
    const is403Error = error.response?.status === 403;
    const is401Error = error.response?.status === 401;
    
    // Log error details for debugging (skip 400/401/403 errors for certain APIs to reduce console noise)
    if (error.response) {
      // Skip logging cart 400/401/403 errors (expected when not authenticated)
      if (isCartApi && (is400Error || is401Error || is403Error)) {
        // Silently handle - don't log to console
        return Promise.reject(error);
      }
      
      // Skip logging notification 401/403 errors (expected when not authenticated)
      if (isNotificationApi && (is401Error || is403Error)) {
        // Silently handle - don't log to console
        return Promise.reject(error);
      }
      
      // Skip logging student API 401/403 errors (expected when not authenticated)
      if (isStudentApi && (is401Error || is403Error)) {
        // Silently handle - don't log to console
        return Promise.reject(error);
      }
      
      // Skip logging content API 400/403 errors (expected when user is not enrolled)
      if (isContentApi && (is400Error || is403Error)) {
        // Silently handle - don't log to console
        // These errors are expected and handled gracefully by the frontend
        return Promise.reject(error);
      }
      
      // Only log non-400/401/403 errors (500, etc.) - validation errors (400) and auth errors (401/403) are handled in UI
      const status = error.response?.status;
      if (status && status !== 400 && status !== 401 && status !== 403) {
        console.error('API Error Response:', {
          status: status,
          statusText: error.response.statusText,
          url: error.config?.url,
          method: error.config?.method,
          data: error.response.data,
          message: error.response.data?.message,
          validationErrors: error.response.data?.validationErrors,
        });
      }
    } else if (error.request) {
      console.error('API Request Error (No Response):', {
        url: error.config?.url,
        method: error.config?.method,
        message: 'No response received from server',
      });
    } else {
      console.error('API Error:', error.message);
    }
    
    // Only redirect on 401 Unauthorized (not 403 Forbidden or other errors)
    if (error.response?.status === 401) {
      // Token hết hạn hoặc không hợp lệ
      if (typeof window !== 'undefined') {
        const currentPath = window.location.pathname;
        
        // Don't redirect if already on login or auth pages
        if (currentPath === '/login' || currentPath.startsWith('/auth/')) {
          // Just clear the data, don't redirect
          localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.USER_DATA);
          // Clear cookie
          document.cookie = 'token=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;';
          return Promise.reject(error);
        }
        
        // Clear auth data from both localStorage and cookie
        localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER_DATA);
        document.cookie = 'token=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;';
        
        // Use replace to avoid adding to history
        window.location.replace('/login');
      }
    }
    // Do NOT redirect on 403 or other errors - let the component handle it
    return Promise.reject(error);
  }
);

export default apiClient;

// Helper functions for cookie management
const setCookie = (name: string, value: string, days: number = 7) => {
  if (typeof document !== 'undefined') {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/;SameSite=Lax`;
  }
};

const removeCookie = (name: string) => {
  if (typeof document !== 'undefined') {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`;
  }
};

// Helper functions
export const setAuthToken = (token: string) => {
  if (typeof window !== 'undefined') {
    localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token);
    // Also set cookie for middleware
    setCookie('token', token, 7); // 7 days expiration
  }
};

export const removeAuthToken = () => {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
    localStorage.removeItem(STORAGE_KEYS.USER_DATA);
    // Also remove cookie
    removeCookie('token');
  }
};

export const getAuthToken = () => {
  if (typeof window !== 'undefined') {
    return localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
  }
  return null;
};

// API Error Handler
export const handleApiError = (error: any): string => {
  if (error.response) {
    // Server responded with error
    return error.response.data?.message || 'Đã có lỗi xảy ra từ server';
  } else if (error.request) {
    // Request made but no response
    return 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng';
  } else {
    // Something else happened
    return error.message || 'Đã có lỗi xảy ra';
  }
};

