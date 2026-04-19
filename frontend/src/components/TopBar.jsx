import { Bell, ChevronDown, LogOut, User } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { useAuthStore } from '../stores/authStore';

export default function TopBar({ user }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const navigate = useNavigate();
  const clearSession = useAuthStore((s) => s.clearSession);

  useEffect(() => {
    const h = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', h);
    return () => document.removeEventListener('mousedown', h);
  }, []);

  const logout = async () => {
    try {
      await api.post('/auth/logout', {});
    } catch {
      /* ignore */
    }
    clearSession();
    navigate('/login');
  };

  const role = user?.role;
  const badge =
    role === 'ADMIN'
      ? 'bg-blue-100 text-blue-800 ring-1 ring-blue-200'
      : 'bg-slate-100 text-slate-700 ring-1 ring-slate-200';

  return (
    <header className="sticky top-0 z-20 border-b border-bordercard bg-white/95 backdrop-blur">
      <div className="flex items-center justify-between px-4 py-3 md:px-6 lg:px-8">
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-slate-500">Organization</p>
          <p className="text-sm font-semibold text-slate-900">{user?.organizationName ?? '—'}</p>
        </div>
        <div className="flex items-center gap-3">
          <span
            className={`hidden rounded-full px-2.5 py-0.5 text-xs font-semibold sm:inline-flex ${badge}`}
          >
            {role}
          </span>
          <button
            type="button"
            className="rounded-full p-2 text-slate-500 hover:bg-slate-100"
            aria-label="Notifications"
          >
            <Bell className="h-5 w-5" />
          </button>
          <div className="relative" ref={ref}>
            <button
              type="button"
              onClick={() => setOpen(!open)}
              className="flex items-center gap-2 rounded-full border border-bordercard py-1 pl-1 pr-2 hover:bg-slate-50"
            >
              <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-700">
                {user?.fullName?.charAt(0) ?? '?'}
              </span>
              <ChevronDown className="h-4 w-4 text-slate-500" />
            </button>
            {open && (
              <div className="absolute right-0 mt-2 w-48 rounded-lg border border-bordercard bg-white py-1 shadow-lg">
                <button
                  type="button"
                  className="flex w-full items-center gap-2 px-3 py-2 text-sm text-slate-700 hover:bg-slate-50"
                  onClick={() => {
                    setOpen(false);
                    navigate('/settings');
                  }}
                >
                  <User className="h-4 w-4" />
                  Profile
                </button>
                <button
                  type="button"
                  className="flex w-full items-center gap-2 px-3 py-2 text-sm text-danger hover:bg-red-50"
                  onClick={logout}
                >
                  <LogOut className="h-4 w-4" />
                  Log out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
