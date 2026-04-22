import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { LogIn, Loader2 } from 'lucide-react';
import { api } from '../api/client';
import { useAuthStore } from '../stores/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const setSessionFromAuth = useAuthStore((s) => s.setSessionFromAuth);
  const [googleError, setGoogleError] = useState('');
  const oauthEndpoint = '/oauth2/authorization/google';

  const [form, setForm] = useState({ email: '', password: '' });
  const [fieldErrors, setFieldErrors] = useState({});
  const [error, setError] = useState('');

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const oauthState = params.get('oauth');
    const accessToken = params.get('accessToken');
    if (oauthState === 'success' && accessToken) {
      setSessionFromAuth(accessToken);
      window.history.replaceState({}, document.title, '/login');
      navigate('/dashboard', { replace: true });
      return;
    }
    if (oauthState === 'error') {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setGoogleError(params.get('message') || 'Google login failed');
      window.history.replaceState({}, document.title, '/login');
    }
  }, [navigate, setSessionFromAuth]);

  const loginMutation = useMutation({
    mutationFn: (payload) => api.post('/auth/login', payload, { skipGlobalErrorToast: true }),
    onSuccess: (res) => {
      const d = res.data;
      setSessionFromAuth(d.accessToken);
      navigate('/dashboard');
    },
    onError: (err) => {
      const det = err.normalized?.details;
      if (det?.length) {
        const fe = {};
        det.forEach((x) => {
          if (x.field) fe[x.field] = x.message;
        });
        setFieldErrors(fe);
      }
      setError(err.normalized?.message || err.message || 'Login failed');
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    loginMutation.mutate(form);
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
      <div className="w-full max-w-md rounded-lg border border-bordercard bg-white p-6 shadow-sm sm:p-8">
        <div className="mb-8 flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/10 text-accent">
            <LogIn className="h-5 w-5" />
          </div>
          <div>
            <h1 className="text-xl font-semibold text-slate-900">Sign in</h1>
            <p className="text-sm text-slate-500">TaskEngine</p>
          </div>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="email" className="mb-1 block text-sm font-medium text-slate-700">
              Email
            </label>
            <input
              id="email"
              type="email"
              autoComplete="email"
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm shadow-sm focus:border-accent focus:ring-2 focus:ring-accent/20"
              value={form.email}
              onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
              required
            />
            {fieldErrors.email && (
              <p className="mt-1 text-xs text-danger">{fieldErrors.email}</p>
            )}
          </div>
          <div>
            <label htmlFor="password" className="mb-1 block text-sm font-medium text-slate-700">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm shadow-sm focus:border-accent focus:ring-2 focus:ring-accent/20"
              value={form.password}
              onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
              required
            />
            {fieldErrors.password && (
              <p className="mt-1 text-xs text-danger">{fieldErrors.password}</p>
            )}
          </div>
          {error && (
            <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-danger" role="alert">
              {error}
            </p>
          )}
          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-accent py-2.5 text-sm font-semibold text-white hover:bg-blue-600 disabled:opacity-60"
          >
            {loginMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
            Sign in
          </button>
        </form>
        <div className="mt-6">
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-white px-2 text-slate-500">Or continue with</span>
            </div>
          </div>
          <div className="mt-4">
            <a
              href={oauthEndpoint}
              className="flex w-full items-center justify-center rounded-lg border border-bordercard bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              Continue with Google
            </a>
          </div>
          {googleError && (
            <p className="mt-2 text-center text-xs text-danger" role="alert">
              {googleError}
            </p>
          )}
        </div>
        <p className="mt-8 text-center text-sm text-slate-600">
          No account? <Link to="/register" className="font-medium text-accent hover:underline">Register</Link>
        </p>
      </div>
    </div>
  );
}
