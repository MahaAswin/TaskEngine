import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import { useOutletContext } from 'react-router-dom';

export default function SettingsPage() {
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const [orgName, setOrgName] = useState('');
  const [profile, setProfile] = useState({
    fullName: '',
    avatarUrl: '',
  });

  useEffect(() => {
    if (!user) return;
    setOrgName(user.organizationName ?? '');
    setProfile({
      fullName: user.fullName ?? '',
      avatarUrl: user.avatarUrl ?? '',
    });
  }, [user]);
  const [pw, setPw] = useState({ current: '', next: '' });

  const orgMutation = useMutation({
    mutationFn: () => api.patch('/org/settings', { name: orgName }),
  });

  const profileMutation = useMutation({
    mutationFn: () => api.patch('/users/me/profile', profile),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['me'] }),
  });

  const pwMutation = useMutation({
    mutationFn: () =>
      api.patch('/users/me/password', {
        currentPassword: pw.current,
        newPassword: pw.next,
      }),
  });

  return (
    <div className="mx-auto max-w-2xl space-y-10">
      <h1 className="text-2xl font-semibold text-slate-900">Settings</h1>
      {user?.role === 'ADMIN' && (
        <section className="rounded-lg border border-bordercard bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900">Organization</h2>
          <label className="mt-4 block text-sm font-medium text-slate-700">Name</label>
          <input
            className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
            value={orgName}
            onChange={(e) => setOrgName(e.target.value)}
          />
          <button
            type="button"
            className="mt-4 rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white"
            onClick={() => orgMutation.mutate()}
          >
            Save organization
          </button>
        </section>
      )}
      <section className="rounded-lg border border-bordercard bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">Profile</h2>
        <label className="mt-4 block text-sm font-medium text-slate-700">Full name</label>
        <input
          className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
          value={profile.fullName}
          onChange={(e) => setProfile((p) => ({ ...p, fullName: e.target.value }))}
        />
        <label className="mt-4 block text-sm font-medium text-slate-700">Avatar URL</label>
        <input
          className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
          value={profile.avatarUrl}
          onChange={(e) => setProfile((p) => ({ ...p, avatarUrl: e.target.value }))}
        />
        <button
          type="button"
          className="mt-4 rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white"
          onClick={() => profileMutation.mutate()}
        >
          Save profile
        </button>
      </section>
      <section className="rounded-lg border border-bordercard bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">Password</h2>
        <label className="mt-4 block text-sm font-medium text-slate-700">Current</label>
        <input
          type="password"
          className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
          value={pw.current}
          onChange={(e) => setPw((p) => ({ ...p, current: e.target.value }))}
        />
        <label className="mt-4 block text-sm font-medium text-slate-700">New</label>
        <input
          type="password"
          className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
          value={pw.next}
          onChange={(e) => setPw((p) => ({ ...p, next: e.target.value }))}
        />
        <button
          type="button"
          className="mt-4 rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white"
          onClick={() => pwMutation.mutate()}
        >
          Update password
        </button>
      </section>
    </div>
  );
}
