import { create } from 'zustand';
import { decodeRoleFromAccessToken } from '../lib/jwt';

export const useAuthStore = create((set, get) => ({
  accessToken: localStorage.getItem('taskengine_access'),
  jwtRole: decodeRoleFromAccessToken(localStorage.getItem('taskengine_access')),

  setSessionFromAuth: (accessToken) => {
    const jwtRole = decodeRoleFromAccessToken(accessToken);
    localStorage.setItem('taskengine_access', accessToken);
    set({ accessToken, jwtRole });
  },

  clearSession: () => {
    localStorage.removeItem('taskengine_access');
    set({ accessToken: null, jwtRole: null });
  },

  isAdminFromJwt: () => get().jwtRole === 'ADMIN',
}));
