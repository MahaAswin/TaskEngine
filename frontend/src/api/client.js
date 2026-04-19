import axios from 'axios';
import toast from 'react-hot-toast';
import { useAuthStore } from '../stores/authStore';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (body && typeof body.success === 'boolean') {
      if (!body.success) {
        return Promise.reject({
          response: { data: body, status: response.status },
        });
      }
      response.data = body.data !== undefined ? body.data : body;
    }
    return response;
  },
  (error) => {
    const status = error.response?.status;
    const data = error.response?.data;
    if (data && typeof data.success === 'boolean' && !data.success) {
      error.normalized = {
        error: data.error,
        message: data.message,
        details: data.details,
      };
    }
    if (status === 401) {
      useAuthStore.getState().clearSession();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    } else if (!error.config?.skipGlobalErrorToast && status && status !== 401) {
      const msg =
        error.normalized?.message ||
        (typeof data?.message === 'string' ? data.message : null) ||
        error.message;
      if (msg) {
        toast.error(msg);
      }
    }
    return Promise.reject(error);
  },
);

export { api };
