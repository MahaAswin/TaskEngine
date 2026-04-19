import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../api/client';
import { useUiStore } from '../stores/uiStore';

export default function CreateTaskModal({ user }) {
  const open = useUiStore((s) => s.createTaskOpen);
  const setOpen = useUiStore((s) => s.setCreateTaskOpen);
  const qc = useQueryClient();
  const isAdmin = user?.role === 'ADMIN';

  const [form, setForm] = useState({
    title: '',
    description: '',
    status: 'TODO',
    priority: 'MEDIUM',
    dueDate: '',
    assignedToId: '',
  });

  const create = useMutation({
    mutationFn: (payload) => api.post('/tasks', payload, { skipGlobalErrorToast: true }),
    onSuccess: () => {
      toast.success('Task created successfully');
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task-stats'] });
      setOpen(false);
      setForm({
        title: '',
        description: '',
        status: 'TODO',
        priority: 'MEDIUM',
        dueDate: '',
        assignedToId: '',
      });
    },
    onError: (err) => {
      toast.error(err.normalized?.message || err.message || 'Could not create task');
    },
  });

  if (!open) return null;

  const submit = (e) => {
    e.preventDefault();
    const aid = String(form.assignedToId).trim();
    create.mutate({
      title: form.title,
      description: form.description || undefined,
      status: form.status,
      priority: form.priority,
      dueDate: form.dueDate || null,
      assignedToId: isAdmin && aid ? aid : null,
    });
  };

  return (
    <div
      className="fixed inset-0 z-[90] flex items-end justify-center bg-slate-900/50 p-0 sm:items-center sm:p-4"
      role="presentation"
    >
      <button
        type="button"
        className="absolute inset-0 cursor-default"
        aria-label="Close dialog"
        onClick={() => setOpen(false)}
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="create-task-title"
        className="relative flex max-h-[95vh] w-full max-w-lg flex-col overflow-hidden rounded-t-2xl border border-bordercard bg-white shadow-xl sm:max-h-[90vh] sm:rounded-2xl"
      >
        <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 sm:px-6">
          <h2 id="create-task-title" className="text-lg font-semibold text-slate-900">
            New task
          </h2>
          <button
            type="button"
            className="rounded-lg p-2 text-slate-500 hover:bg-slate-100"
            aria-label="Close"
            onClick={() => setOpen(false)}
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={submit} className="overflow-y-auto px-4 py-4 sm:px-6">
          <div className="space-y-4">
            <div>
              <label htmlFor="ct-title" className="text-sm font-medium text-slate-700">
                Title
              </label>
              <input
                id="ct-title"
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                required
              />
            </div>
            <div>
              <label htmlFor="ct-desc" className="text-sm font-medium text-slate-700">
                Description
              </label>
              <textarea
                id="ct-desc"
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                rows={3}
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label htmlFor="ct-status" className="text-sm font-medium text-slate-700">
                  Status
                </label>
                <select
                  id="ct-status"
                  className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                  value={form.status}
                  onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))}
                >
                  <option value="TODO">To do</option>
                  <option value="IN_PROGRESS">In progress</option>
                  <option value="IN_REVIEW">In review</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
              <div>
                <label htmlFor="ct-priority" className="text-sm font-medium text-slate-700">
                  Priority
                </label>
                <select
                  id="ct-priority"
                  className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                  value={form.priority}
                  onChange={(e) => setForm((f) => ({ ...f, priority: e.target.value }))}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
            </div>
            <div>
              <label htmlFor="ct-due" className="text-sm font-medium text-slate-700">
                Due date
              </label>
              <input
                id="ct-due"
                type="date"
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                value={form.dueDate}
                onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value }))}
              />
            </div>
            {isAdmin && (
              <div>
                <label htmlFor="ct-assign" className="text-sm font-medium text-slate-700">
                  Assignee user ID
                </label>
                <input
                  id="ct-assign"
                  className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 font-mono text-xs"
                  value={form.assignedToId}
                  onChange={(e) => setForm((f) => ({ ...f, assignedToId: e.target.value }))}
                  placeholder="Optional UUID"
                />
              </div>
            )}
          </div>
          <div className="mt-6 flex justify-end gap-2 border-t border-slate-100 pt-4">
            <button
              type="button"
              className="rounded-lg border border-bordercard px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
              onClick={() => setOpen(false)}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={create.isPending}
              className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-blue-600 disabled:opacity-60"
            >
              {create.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden /> : null}
              Create task
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
