import { useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { Loader2 } from 'lucide-react';
import { api } from '../api/client';
import { useAuthStore } from '../stores/authStore';

function strengthScore(pw) {
  let s = 0;
  if (pw.length >= 8) s++;
  if (/[a-z]/.test(pw) && /[A-Z]/.test(pw)) s++;
  if (/\d/.test(pw)) s++;
  if (/[^A-Za-z0-9]/.test(pw)) s++;
  return Math.min(s, 3);
}

export default function RegisterPage() {
  const navigate = useNavigate();
  const setSessionFromAuth = useAuthStore((s) => s.setSessionFromAuth);
  const [form, setForm] = useState({
    organizationName: '',
    fullName: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');

  const score = useMemo(() => strengthScore(form.password), [form.password]);
  const labels = ['Weak', 'Fair', 'Good', 'Strong'];

  const reg = useMutation({
    mutationFn: (payload) => api.post('/auth/register', payload, { skipGlobalErrorToast: true }),
    onSuccess: (res) => {
      setSessionFromAuth(res.data.accessToken);
      navigate('/dashboard');
    },
    onError: (err) => setError(err.normalized?.message || err.message || 'Registration failed'),
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');
    reg.mutate({
      organizationName: form.organizationName,
      fullName: form.fullName,
      email: form.email,
      password: form.password,
    });
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-12">
      <div className="w-full max-w-md rounded-lg border border-bordercard bg-white p-6 shadow-sm sm:p-8">
        <h1 className="text-xl font-semibold text-slate-900">Create workspace</h1>
        <p className="mt-1 text-sm text-slate-500">You will be the organization admin.</p>
        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Organization</label>
            <input
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm"
              value={form.organizationName}
              onChange={(e) => setForm((p) => ({ ...p, organizationName: e.target.value }))}
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Full name</label>
            <input
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm"
              value={form.fullName}
              onChange={(e) => setForm((p) => ({ ...p, fullName: e.target.value }))}
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Email</label>
            <input
              type="email"
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm"
              value={form.email}
              onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Password</label>
            <input
              type="password"
              className="w-full rounded-lg border border-bordercard px-3 py-2.5 text-sm"
              value={form.password}
              onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
              required
              minLength={8}
            />
            <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className={`h-full transition-all ${
                  score <= 0 ? 'w-1/4 bg-danger'
                  : score === 1 ? 'w-2/4 bg-warning'
                  : score === 2 ? 'w-3/4 bg-accent'
                  : 'w-full bg-success'
                }`}
              />
            </div>
            <p className="mt-1 text-xs text-slate-500">Strength: {labels[score]}</p>
          </div>
          {error && <p className="text-sm text-danger">{error}</p>}
          <button
            type="submit"
            disabled={reg.isPending}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-accent py-2.5 text-sm font-semibold text-white disabled:opacity-60"
          >
            {reg.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
            Create workspace
          </button>
        </form>
        <p className="mt-8 text-center text-sm text-slate-600">
          <Link to="/login" className="font-medium text-accent hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
