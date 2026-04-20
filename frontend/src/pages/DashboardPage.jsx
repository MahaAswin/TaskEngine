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
  const openCreateTask = useUiStore((s) => s.openCreateTask);
  const qc = useQueryClient();
  const [status, setStatus] = useState('');
  const [scopeTab, setScopeTab] = useState('ALL');

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

  const { data: allTasks = [] } = useQuery({
    queryKey: ['tasks', 'dash-all'],
    queryFn: async () => {
      const params = new URLSearchParams({ page: '0', size: '100' });
      const { data } = await api.get(`/tasks?${params}`);
      return data.content || [];
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

  const scopeCounts = allTasks.reduce(
    (acc, t) => {
      acc.all += 1;
      if (t.scope === 'GLOBAL') acc.global += 1;
      if (t.scope === 'TEAM') acc.team += 1;
      if (t.scope === 'PRIVATE') acc.private += 1;
      return acc;
    },
    { all: 0, global: 0, team: 0, private: 0 },
  );

  const cards = [
    { label: 'All Visible Tasks', value: scopeCounts.all, tone: 'text-slate-900' },
    { label: 'Global', value: scopeCounts.global, tone: 'text-blue-700' },
    { label: 'Team', value: scopeCounts.team, tone: 'text-indigo-700' },
    { label: 'Private', value: scopeCounts.private, tone: 'text-purple-700' },
  ];

  const scopedTasks = (tasksPage?.content || []).filter((t) => {
    if (scopeTab === 'ALL') return true;
    return t.scope === scopeTab;
  });

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
          <div className="flex flex-wrap items-center gap-2">
            <div className="inline-flex rounded-lg bg-slate-100 p-1">
              {['ALL', 'GLOBAL', 'TEAM', 'PRIVATE'].map((tab) => (
                <button
                  key={tab}
                  type="button"
                  className={`rounded-md px-3 py-1 text-xs font-medium ${scopeTab === tab ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'}`}
                  onClick={() => setScopeTab(tab)}
                >
                  {tab === 'ALL' ? 'All' : tab}
                </button>
              ))}
            </div>
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
        </div>
        {loading ? (
          <div className="grid gap-3 md:grid-cols-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-36 animate-pulse rounded-lg bg-slate-200/90" />
            ))}
          </div>
        ) : scopedTasks.length ? (
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            {scopedTasks.map((t) => (
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
                onClick={() => openCreateTask()}
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
