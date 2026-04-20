import { Bell } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { useEffect, useRef, useState } from 'react';
import { useNotificationStore } from '../stores/useNotificationStore';

const typeStyles = {
  TASK_CREATED: 'border-l-emerald-500',
  TASK_UPDATED: 'border-l-blue-500',
  TASK_DELETED: 'border-l-red-500',
  TASK_STATUS_CHANGED: 'border-l-amber-500',
};

function badgeText(count) {
  if (count > 9) return '9+';
  return String(count);
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const notifications = useNotificationStore((s) => s.notifications);
  const unreadCount = useNotificationStore((s) => s.unreadCount);
  const markAllRead = useNotificationStore((s) => s.markAllRead);
  const latest = notifications.slice(0, 10);

  useEffect(() => {
    const onOutside = (event) => {
      if (ref.current && !ref.current.contains(event.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', onOutside);
    return () => document.removeEventListener('mousedown', onOutside);
  }, []);

  return (
    <div className="relative" ref={ref}>
      <button
        type="button"
        className="relative rounded-full p-2 text-slate-500 hover:bg-slate-100"
        aria-label="Notifications"
        onClick={() => setOpen((v) => !v)}
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-bold text-white">
            {badgeText(unreadCount)}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 z-40 mt-2 w-80 rounded-xl border border-bordercard bg-white p-3 shadow-xl">
          <div className="mb-2 flex items-center justify-between">
            <p className="text-sm font-semibold text-slate-900">Notifications</p>
            <button
              type="button"
              className="text-xs font-medium text-accent hover:underline"
              onClick={markAllRead}
            >
              Mark all read
            </button>
          </div>
          {latest.length ? (
            <div className="max-h-96 space-y-2 overflow-y-auto">
              {latest.map((n) => (
                <div
                  key={n.id}
                  className={`rounded-lg border border-slate-200 border-l-4 bg-white p-2 ${
                    typeStyles[n.type] || 'border-l-slate-400'
                  }`}
                >
                  <p className="text-sm text-slate-800">{n.message}</p>
                  <p className="mt-1 text-[11px] text-slate-500">
                    {n.timestamp
                      ? formatDistanceToNow(new Date(n.timestamp), { addSuffix: true })
                      : 'just now'}
                  </p>
                </div>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center gap-2 py-8 text-center text-sm text-slate-500">
              <Bell className="h-5 w-5 text-slate-400" />
              <p>No notifications yet</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
