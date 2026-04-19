import { create } from 'zustand';
import { decodeRoleFromAccessToken } from '../lib/jwt';

export const useAuthStore = create((set, get) => ({
  accessToken: null,
  jwtRole: null,

  setSessionFromAuth: (accessToken) => {
    const jwtRole = decodeRoleFromAccessToken(accessToken);
    set({ accessToken, jwtRole });
  },

  clearSession: () => set({ accessToken: null, jwtRole: null }),

  isAdminFromJwt: () => get().jwtRole === 'ADMIN',
}));
