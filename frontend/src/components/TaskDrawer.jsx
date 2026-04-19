import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { X, ExternalLink } from 'lucide-react';
import { api } from '../api/client';
import { useUiStore } from '../stores/uiStore';
import StatusBadge from './StatusBadge';
import PriorityBadge from './PriorityBadge';

export default function TaskDrawer() {
  const navigate = useNavigate();
  const taskId = useUiStore((s) => s.taskDrawerId);
  const setTaskDrawerId = useUiStore((s) => s.setTaskDrawerId);

  const { data: task, isLoading, isError } = useQuery({
    queryKey: ['task', taskId],
    queryFn: async () => {
      const { data } = await api.get(`/tasks/${taskId}`);
      return data;
    },
    enabled: !!taskId,
    retry: false,
  });

  if (!taskId) return null;

  const onClose = () => setTaskDrawerId(null);

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <button
        type="button"
        className="absolute inset-0 bg-slate-900/40"
        aria-label="Close task panel"
        onClick={onClose}
      />
      <div className="relative flex h-full w-full max-w-md flex-col border-l border-bordercard bg-white shadow-xl max-md:max-h-[90vh] max-md:rounded-t-2xl max-md:border max-md:border-b-0">
        <div className="flex items-center justify-between border-b border-bordercard px-4 py-3">
          <h2 className="text-sm font-semibold text-slate-900">Task preview</h2>
          <button
            type="button"
            className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
            onClick={onClose}
            aria-label="Close"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="flex-1 overflow-y-auto p-4">
          {isLoading && (
            <div className="space-y-3" aria-busy="true">
              <div className="h-6 w-2/3 animate-pulse rounded bg-slate-200" />
              <div className="h-20 w-full animate-pulse rounded bg-slate-200" />
            </div>
          )}
          {isError && (
            <p className="text-sm text-red-600">Could not load this task.</p>
          )}
          {task && (
            <div className="space-y-4">
              <div className="flex flex-wrap gap-2">
                <StatusBadge status={task.status} />
                <PriorityBadge priority={task.priority} />
              </div>
              <h3 className="text-lg font-semibold text-slate-900">{task.title}</h3>
              {task.description && (
                <p className="text-sm text-slate-600">{task.description}</p>
              )}
              <dl className="space-y-2 text-sm">
                <div className="flex justify-between gap-2">
                  <dt className="text-slate-500">Due</dt>
                  <dd className="font-mono text-xs text-slate-800">{task.dueDate ?? '—'}</dd>
                </div>
                <div className="flex justify-between gap-2">
                  <dt className="text-slate-500">Assignee</dt>
                  <dd className="text-slate-800">{task.assignedToName ?? '—'}</dd>
                </div>
                <div className="flex justify-between gap-2">
                  <dt className="text-slate-500">Created by</dt>
                  <dd className="text-slate-800">{task.createdByName}</dd>
                </div>
              </dl>
              <button
                type="button"
                className="inline-flex w-full items-center justify-center gap-2 rounded-lg bg-accent px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-600"
                onClick={() => {
                  setTaskDrawerId(null);
                  navigate(`/tasks/${task.id}`);
                }}
              >
                Open full page
                <ExternalLink className="h-4 w-4" aria-hidden />
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
