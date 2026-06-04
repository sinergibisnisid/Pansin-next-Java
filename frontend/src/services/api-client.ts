import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/constants';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Read token from direct localStorage key first,
 * fallback to Zustand persist storage as safety net.
 */
function getTokenFromStorage(key: string): string | null {
  if (typeof window === 'undefined') return null;

  // 1. Try direct localStorage key
  const directToken = localStorage.getItem(key);
  if (directToken) return directToken;

  // 2. Fallback: read from Zustand persist blob
  try {
    const persistedRaw = localStorage.getItem('pansis-auth-storage');
    if (persistedRaw) {
      const persisted = JSON.parse(persistedRaw);
      const tokens = persisted?.state?.tokens;
      if (tokens) {
        if (key === TOKEN_KEY && tokens.accessToken) return tokens.accessToken;
        if (key === REFRESH_TOKEN_KEY && tokens.refreshToken) return tokens.refreshToken;
      }
    }
  } catch {
    // JSON parse failed, ignore
  }

  return null;
}

// Request interceptor - attach token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getTokenFromStorage(TOKEN_KEY);
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - handle token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = getTokenFromStorage(REFRESH_TOKEN_KEY);
        if (!refreshToken) {
          handleLogout();
          return Promise.reject(error);
        }

        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { accessToken, refreshToken: newRefreshToken } = response.data.data;
        localStorage.setItem(TOKEN_KEY, accessToken);
        localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }
        return apiClient(originalRequest);
      } catch {
        handleLogout();
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

function handleLogout() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem('pansis-auth-storage');
    window.location.href = '/login';
  }
}

export default apiClient;
