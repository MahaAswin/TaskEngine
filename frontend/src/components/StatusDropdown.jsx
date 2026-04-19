import { useState, useRef, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ChevronDown } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../api/client';

const STATUSES = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];

const styles = {
  TODO: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
  IN_PROGRESS: 'bg-blue-50 text-blue-800 ring-1 ring-blue-200',
  IN_REVIEW: 'bg-amber-50 text-amber-900 ring-1 ring-amber-200',
  DONE: 'bg-emerald-50 text-emerald-800 ring-1 ring-emerald-200',
};

export default function StatusDropdown({ taskId, status, disabled }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const qc = useQueryClient();

  useEffect(() => {
    const h = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', h);
    return () => document.removeEventListener('mousedown', h);
  }, []);

  const mutation = useMutation({
    mutationFn: (next) =>
      api.patch(`/tasks/${taskId}/status`, { status: next }, { skipGlobalErrorToast: true }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task', taskId] });
      qc.invalidateQueries({ queryKey: ['task-stats'] });
      qc.invalidateQueries({ queryKey: ['task-history', taskId] });
      setOpen(false);
    },
    onError: (err) => {
      toast.error(err.normalized?.message || err.message || 'Could not update status');
    },
  });

  const cls = styles[status] || styles.TODO;
  const label = status?.replace(/_/g, ' ') ?? '';

  return (
    <div className="relative inline-block text-left" ref={ref}>
      <button
        type="button"
        disabled={disabled || mutation.isPending}
        onClick={() => !disabled && setOpen((o) => !o)}
        className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium capitalize transition hover:ring-2 hover:ring-accent/30 ${cls}`}
        aria-label={`Status: ${label}. Change status`}
        aria-expanded={open}
        aria-haspopup="listbox"
      >
        {label}
        <ChevronDown className="h-3 w-3 opacity-70" aria-hidden />
      </button>
      {open && (
        <ul
          className="absolute left-0 z-50 mt-1 min-w-[10rem] rounded-lg border border-bordercard bg-white py-1 shadow-lg"
          role="listbox"
          aria-label="Choose status"
        >
          {STATUSES.map((s) => (
            <li key={s} role="option" aria-selected={s === status}>
              <button
                type="button"
                className={`flex w-full px-3 py-2 text-left text-sm capitalize hover:bg-slate-50 ${
                  s === status ? 'font-semibold text-accent' : 'text-slate-700'
                }`}
                onClick={() => mutation.mutate(s)}
              >
                {s.replace(/_/g, ' ')}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
