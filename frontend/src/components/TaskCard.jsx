import { useNavigate } from 'react-router-dom';
import { Calendar, Pencil, Trash2, User } from 'lucide-react';
import StatusDropdown from './StatusDropdown';
import PriorityBadge from './PriorityBadge';
import { useUiStore } from '../stores/uiStore';

function canMutateTask(user, task, jwtRole) {
  const admin = user?.role === 'ADMIN' || jwtRole === 'ADMIN';
  if (admin) return true;
  if (user?.id && task?.createdBy) return String(user.id) === String(task.createdBy);
  return false;
}

export default function TaskCard({ task, user, jwtRole, onDelete }) {
  const navigate = useNavigate();
  const openConfirm = useUiStore((s) => s.openConfirm);
  const can = canMutateTask(user, task, jwtRole);

  const scopeAccent =
    task.scope === 'GLOBAL'
      ? 'border-l-4 border-l-blue-500'
      : task.scope === 'PRIVATE'
        ? 'border-l-4 border-l-purple-500'
        : 'border-l-4 border-l-indigo-500';

  return (
    <div className={`group relative w-full rounded-lg border border-bordercard bg-white text-left shadow-sm transition hover:border-slate-300 hover:shadow-md ${scopeAccent}`}>
      <div
        role="link"
        tabIndex={0}
        onClick={() => navigate(`/tasks/${task.id}`)}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            navigate(`/tasks/${task.id}`);
          }
        }}
        className="block w-full cursor-pointer p-4 text-left outline-none focus-visible:ring-2 focus-visible:ring-accent"
        aria-label={`Open task ${task.title}`}
      >
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="font-semibold text-slate-900 line-clamp-2">{task.title}</h3>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <PriorityBadge priority={task.priority} />
              <span className="rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-semibold text-slate-600">{task.scope}</span>
            </div>
          </div>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-slate-500">
          {task.assignedToName && (
            <span className="inline-flex items-center gap-1">
              <User className="h-3.5 w-3.5" aria-hidden />
              {task.assignedToName}
            </span>
          )}
          {task.dueDate && (
            <span className="inline-flex items-center gap-1 font-mono">
              <Calendar className="h-3.5 w-3.5" aria-hidden />
              {task.dueDate}
            </span>
          )}
        </div>
      </div>
      <div
        className="pointer-events-none absolute right-2 top-2 flex items-center gap-1 opacity-0 transition-opacity duration-150 group-hover:pointer-events-auto group-hover:opacity-100 max-sm:pointer-events-auto max-sm:opacity-100"
        onClick={(e) => e.stopPropagation()}
      >
        <div onClick={(e) => e.stopPropagation()} className="rounded-md bg-white/90 p-0.5 shadow-sm ring-1 ring-slate-200/80 backdrop-blur">
          <StatusDropdown taskId={task.id} status={task.status} disabled={task.deleted || !can} />
        </div>
        {can && (
          <>
            <button
              type="button"
              className="rounded-md bg-white/90 p-2 text-slate-600 shadow-sm ring-1 ring-slate-200/80 hover:text-accent"
              aria-label="Edit task"
              onClick={() => navigate(`/tasks/${task.id}`)}
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              type="button"
              className="rounded-md bg-white/90 p-2 text-slate-600 shadow-sm ring-1 ring-slate-200/80 hover:text-danger"
              aria-label="Delete task"
              onClick={() =>
                openConfirm({
                  title: 'Delete task',
                  message: `Delete “${task.title}”?`,
                  confirmLabel: 'Delete',
                  onConfirm: () => onDelete?.(task),
                })
              }
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </>
        )}
      </div>
    </div>
  );
}
