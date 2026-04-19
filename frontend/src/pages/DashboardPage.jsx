import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import { api } from '../api/client';
import TaskCard from '../components/TaskCard';
import EmptyState from '../components/EmptyState';
import { StatCardsSkeleton } from '../components/skeletons';
import { useAuthStore } from '../stores/authStore';
import { useUiStore } from '../stores/uiStore';

export default function DashboardPage() {
  const { user } = useOutletContext();
  const jwtRole = useAuthStore((s) => s.jwtRole);
  const setCreateTaskOpen = useUiStore((s) => s.setCreateTaskOpen);
  const qc = useQueryClient();
  const [status, setStatus] = useState('');

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['task-stats'],
    queryFn: async () => {
      const { data } = await api.get('/tasks/stats');
      return data;
    },
  });

  const { data: tasksPage, isLoading: tasksLoading } = useQuery({
    queryKey: ['tasks', 'dash', status],
    queryFn: async () => {
      const params = new URLSearchParams({ page: '0', size: '8', sort: 'createdAt,desc' });
      if (status) params.set('status', status);
      const { data } = await api.get(`/tasks?${params}`);
      return data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => api.delete(`/tasks/${taskId}`),
    onSuccess: (_, taskId) => {
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task-stats'] });
      toast.custom((t) => (
        <div className="flex max-w-sm items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-lg">
          <span className="text-sm font-medium text-slate-800">Task deleted</span>
          <button
            type="button"
            className="rounded-md bg-slate-900 px-3 py-1 text-xs font-semibold text-white hover:bg-slate-800"
            onClick={async () => {
              toast.dismiss(t);
              try {
                await api.post(`/tasks/${taskId}/restore`, {}, { skipGlobalErrorToast: true });
                toast.success('Task restored');
                qc.invalidateQueries({ queryKey: ['tasks'] });
                qc.invalidateQueries({ queryKey: ['task-stats'] });
              } catch (err) {
                toast.error(err.normalized?.message || err.message || 'Could not undo');
              }
            }}
          >
            Undo
          </button>
        </div>
      ), { duration: 8000 });
    },
  });

  const cards = [
    { label: 'Total', value: stats?.total ?? 0, tone: 'text-slate-900' },
    { label: 'To do', value: stats?.todo ?? 0, tone: 'text-slate-700' },
    { label: 'In progress', value: stats?.inProgress ?? 0, tone: 'text-blue-700' },
    { label: 'Done', value: stats?.done ?? 0, tone: 'text-emerald-700' },
  ];

  const loading = statsLoading || tasksLoading;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Dashboard</h1>
        <p className="text-sm text-slate-500">Welcome back, {user?.fullName}</p>
      </div>
      {statsLoading ? (
        <StatCardsSkeleton />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {cards.map((c) => (
            <div
              key={c.label}
              className="rounded-lg border border-bordercard bg-white p-4 shadow-sm"
            >
              <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{c.label}</p>
              <p className={`mt-2 text-3xl font-semibold ${c.tone}`}>{c.value}</p>
            </div>
          ))}
        </div>
      )}
      <div>
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-lg font-semibold text-slate-900">Recent tasks</h2>
          <select
            className="rounded-lg border border-bordercard bg-white px-3 py-2 text-sm"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            aria-label="Filter by status"
          >
            <option value="">All statuses</option>
            <option value="TODO">To do</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="IN_REVIEW">In review</option>
            <option value="DONE">Done</option>
          </select>
        </div>
        {loading ? (
          <div className="grid gap-3 md:grid-cols-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-36 animate-pulse rounded-lg bg-slate-200/90" />
            ))}
          </div>
        ) : tasksPage?.content?.length ? (
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            {tasksPage.content.map((t) => (
              <TaskCard
                key={t.id}
                task={t}
                user={user}
                jwtRole={jwtRole}
                onDelete={(task) => deleteMutation.mutate(task.id)}
              />
            ))}
          </div>
        ) : (
          <EmptyState
            title="No tasks yet"
            description="Create your first task to see it here."
            action={
              <button
                type="button"
                className="rounded-lg bg-accent px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-600"
                onClick={() => setCreateTaskOpen(true)}
              >
                Create your first task
              </button>
            }
          />
        )}
      </div>
    </div>
  );
}
