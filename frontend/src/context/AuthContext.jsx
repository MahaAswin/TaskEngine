import { useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../lib/api';

import { AuthContext } from './auth-context';

const ACCESS_KEY = 'taskengine_access';
const REFRESH_KEY = 'taskengine_refresh';

export function AuthProvider({ children }) {
  const navigate = useNavigate();
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem(ACCESS_KEY));
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem(REFRESH_KEY));
  const [user, setUser] = useState(null);

  const fetchProfile = useCallback(
    async (providedToken) => {
      const t = providedToken || accessToken;
      const profile = await api.get('/users/me', t);
      setUser(profile);
    },
    [accessToken],
  );

  const persistTokens = useCallback((access, refresh) => {
    localStorage.setItem(ACCESS_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
    setAccessToken(access);
    setRefreshToken(refresh);
  }, []);

  const login = useCallback(
    async (payload) => {
      const response = await api.post('/auth/login', payload);
      persistTokens(response.accessToken, response.refreshToken);
      await fetchProfile(response.accessToken);
      navigate('/dashboard');
    },
    [fetchProfile, navigate, persistTokens],
  );

  const register = useCallback(
    async (payload) => {
      const response = await api.post('/auth/register', payload);
      persistTokens(response.accessToken, response.refreshToken);
      await fetchProfile(response.accessToken);
      navigate('/dashboard');
    },
    [fetchProfile, navigate, persistTokens],
  );

  const loginWithGoogle = useCallback(
    async (idToken) => {
      const response = await api.post('/auth/oauth2/callback', { idToken });
      persistTokens(response.accessToken, response.refreshToken);
      await fetchProfile(response.accessToken);
      navigate('/dashboard');
    },
    [fetchProfile, navigate, persistTokens],
  );

  const logout = useCallback(async () => {
    const rt = localStorage.getItem(REFRESH_KEY);
    try {
      if (rt) {
        await api.post('/auth/logout', { refreshToken: rt });
      }
    } catch {
      // Still clear local session if revoke fails.
    }
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
    navigate('/login');
  }, [navigate]);

  const value = useMemo(
    () => ({
      token: accessToken,
      accessToken,
      refreshToken,
      user,
      login,
      register,
      loginWithGoogle,
      logout,
      fetchProfile,
      isAuthenticated: Boolean(accessToken),
    }),
    [accessToken, refreshToken, user, login, register, loginWithGoogle, logout, fetchProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
