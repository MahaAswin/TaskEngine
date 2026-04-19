import { Building2 } from 'lucide-react';

export default function Navbar({ user }) {
  return (
    <header className="border-b border-bordercard bg-white px-4 py-4 sm:px-6 sm:py-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-lg font-semibold tracking-tight text-slate-900 sm:text-xl">Task dashboard</h2>
          <p className="mt-0.5 text-sm text-slate-500">Plan, track, and ship work across your organization</p>
        </div>
        <div className="flex items-start gap-3 rounded-lg border border-bordercard bg-slate-50/80 px-4 py-3 sm:text-right">
          <Building2 className="mt-0.5 h-5 w-5 shrink-0 text-slate-400" />
          <div className="min-w-0 text-left sm:text-right">
            <p className="truncate text-sm font-semibold text-slate-900">{user?.fullName || 'User'}</p>
            <p className="font-mono text-xs text-slate-500">
              {user?.role}
              {user?.organizationId ? (
                <>
                  {' '}
                  <span className="text-slate-400">·</span> org{' '}
                  <span className="text-slate-600">{user.organizationId}</span>
                </>
              ) : null}
            </p>
          </div>
        </div>
      </div>
    </header>
  );
}
