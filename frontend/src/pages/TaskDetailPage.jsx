import { useState, useEffect } from 'react';
import { useParams, useOutletContext } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Loader2, Globe, Users, Lock } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../api/client';
import StatusBadge from '../components/StatusBadge';
import PriorityBadge from '../components/PriorityBadge';
import ActivityTimeline from '../components/ActivityTimeline';
import { TaskDetailSkeleton } from '../components/skeletons';
import { useAuthStore } from '../stores/authStore';

export default function TaskDetailPage() {
  const { id } = useParams();
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const jwtRole = useAuthStore((s) => s.jwtRole);
  const isAdmin = user?.role === 'ADMIN' || jwtRole === 'ADMIN';

  const { data: task, isLoading, isError } = useQuery({
    queryKey: ['task', id],
    queryFn: async () => {
      const { data } = await api.get(`/tasks/${id}`);
      return data;
    },
    retry: false,
  });

  const { data: history } = useQuery({
    queryKey: ['task-history', id],
    queryFn: async () => {
      const { data } = await api.get(`/tasks/${id}/history`);
      return data;
    },
    enabled: !!task,
    refetchInterval: 30_000,
  });

  const [form, setForm] = useState(null);

  useEffect(() => {
    if (!task) return;
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setForm({
      title: task.title,
      description: task.description ?? '',
      status: task.status,
      priority: task.priority,
      dueDate: task.dueDate ? String(task.dueDate).slice(0, 10) : '',
      assignedToId: task.assignedTo ? String(task.assignedTo) : '',
      scope: task.scope,
      teamId: task.teamId ? String(task.teamId) : '',
    });
  }, [task]);

  const saveMutation = useMutation({
    mutationFn: (payload) => api.put(`/tasks/${id}`, payload, { skipGlobalErrorToast: true }),
    onSuccess: () => {
      toast.success('Task saved');
      qc.invalidateQueries({ queryKey: ['task', id] });
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task-history', id] });
    },
    onError: (err) => {
      toast.error(err.normalized?.message || err.message || 'Could not save');
    },
  });

  const restoreMutation = useMutation({
    mutationFn: () => api.post(`/tasks/${id}/restore`, {}, { skipGlobalErrorToast: true }),
    onSuccess: () => {
      toast.success('Task restored');
      qc.invalidateQueries({ queryKey: ['task', id] });
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task-history', id] });
    },
    onError: (err) => {
      toast.error(err.normalized?.message || err.message || 'Could not restore');
    },
  });

  const mayEdit =
    !task?.deleted &&
    (isAdmin ||
      (user?.id && task?.createdBy && String(user.id) === String(task.createdBy)));

  const canRestore =
    task?.deleted &&
    (isAdmin ||
      (user?.id && task?.createdBy && String(user.id) === String(task.createdBy)));

  const handleSave = (e) => {
    e.preventDefault();
    if (!form) return;
    const aid = String(form.assignedToId ?? '').trim();
    saveMutation.mutate({
      title: form.title,
      description: form.description,
      status: form.status,
      priority: form.priority,
      scope: form.scope,
      teamId: form.scope === 'TEAM' ? form.teamId || null : null,
      dueDate: form.dueDate || null,
      assignedToId: aid ? aid : null,
    });
  };

  if (isLoading) {
    return <TaskDetailSkeleton />;
  }

  if (isError || !task) {
    return <p className="text-sm text-red-600">Task not found or you do not have access.</p>;
  }

  if (!form) {
    return <TaskDetailSkeleton />;
  }

  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_360px]">
      <div className="space-y-6">
        {task.deleted && (
          <div
            className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-900"
            role="alert"
          >
            <span>
              This task was <span className="line-through">deleted</span>. You can restore it if you
              have permission.
            </span>
            {canRestore && (
              <button
                type="button"
                className="rounded-lg bg-red-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-800"
                onClick={() => restoreMutation.mutate()}
                disabled={restoreMutation.isPending}
              >
                {restoreMutation.isPending ? 'Restoring…' : 'Restore task'}
              </button>
            )}
          </div>
        )}
        <div>
          <div className="flex flex-wrap items-center gap-2">
            <StatusBadge status={task.status} />
            <PriorityBadge priority={task.priority} />
            <span className="inline-flex items-center gap-1 rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
              {task.scope === 'GLOBAL' ? <Globe className="h-3.5 w-3.5" /> : task.scope === 'TEAM' ? <Users className="h-3.5 w-3.5" /> : <Lock className="h-3.5 w-3.5" />}
              {task.scope === 'TEAM' ? `TEAM: ${task.teamId ?? 'Unknown'}` : task.scope}
            </span>
          </div>
          <h1
            className={`mt-2 text-2xl font-semibold text-slate-900 ${task.deleted ? 'text-slate-500 line-through' : ''}`}
          >
            {task.title}
          </h1>
          <p className="mt-2 font-mono text-xs text-slate-500">{task.id}</p>
        </div>
        {mayEdit ? (
          <form
            onSubmit={handleSave}
            className="space-y-4 rounded-lg border border-bordercard bg-white p-6 shadow-sm"
          >
            <div>
              <label className="text-sm font-medium text-slate-700">Title</label>
              <input
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                value={form.title}
                onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                required
              />
            </div>
            <div>
              <label className="text-sm font-medium text-slate-700">Description</label>
              <textarea
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                rows={4}
                value={form.description}
                onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              />
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <div>
                <label className="text-sm font-medium text-slate-700">Status</label>
                <select
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
                <label className="text-sm font-medium text-slate-700">Priority</label>
                <select
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
              <label className="text-sm font-medium text-slate-700">Due date</label>
              <input
                type="date"
                className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 text-sm"
                value={form.dueDate}
                onChange={(e) => setForm((f) => ({ ...f, dueDate: e.target.value }))}
              />
            </div>
            {isAdmin && (
              <div>
                <label className="text-sm font-medium text-slate-700">Assignee user ID</label>
                <input
                  className="mt-1 w-full rounded-lg border border-bordercard px-3 py-2 font-mono text-xs"
                  value={form.assignedToId}
                  onChange={(e) => setForm((f) => ({ ...f, assignedToId: e.target.value }))}
                />
              </div>
            )}
            <button
              type="submit"
              disabled={saveMutation.isPending}
              className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-blue-600 disabled:opacity-60"
            >
              {saveMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" aria-hidden /> : null}
              Save changes
            </button>
          </form>
        ) : (
          <p className="text-sm text-slate-500">You have read-only access to this task.</p>
        )}
      </div>
      <div className="lg:border-l lg:border-slate-200 lg:pl-6">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Activity</h2>
        <div className="mt-4">
          <ActivityTimeline entries={history ?? []} />
        </div>
      </div>
    </div>
  );
}
