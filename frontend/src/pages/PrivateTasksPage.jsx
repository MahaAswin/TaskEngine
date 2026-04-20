import { Lock } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useOutletContext } from 'react-router-dom';
import { api } from '../api/client';
import TaskCard from '../components/TaskCard';
import EmptyState from '../components/EmptyState';
import { useUiStore } from '../stores/uiStore';

export default function PrivateTasksPage() {
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const openCreateTask = useUiStore((s) => s.openCreateTask);

  const { data, isLoading } = useQuery({
    queryKey: ['private-tasks'],
    queryFn: async () => {
      const params = new URLSearchParams({ page: '0', size: '50' });
      const { data } = await api.get(`/tasks?${params}`);
      return (data.content || []).filter((t) => t.scope === 'PRIVATE');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => api.delete(`/tasks/${taskId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['private-tasks'] }),
  });

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">My Tasks</h1>
          <p className="text-sm text-slate-500">Personal private workspace.</p>
        </div>
        <button
          type="button"
          onClick={() => openCreateTask({ scope: 'PRIVATE' })}
          className="rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-blue-600"
        >
          + Create Private Task
        </button>
      </div>
      <p className="rounded-lg border border-purple-200 bg-purple-50 px-4 py-2 text-sm text-purple-900">
        <span className="inline-flex items-center gap-2"><Lock className="h-4 w-4" />Only you can see these tasks.</span>
      </p>
      {isLoading ? (
        <div className="grid gap-3 md:grid-cols-2">{[1, 2, 3].map((i) => <div key={i} className="h-36 animate-pulse rounded-lg bg-slate-200" />)}</div>
      ) : data?.length ? (
        <div className="grid gap-3 md:grid-cols-2">
          {data.map((task) => (
            <div key={task.id} className="rounded-lg border-l-4 border-l-purple-500">
              <TaskCard task={task} user={user} jwtRole={user?.role} onDelete={(t) => deleteMutation.mutate(t.id)} />
            </div>
          ))}
        </div>
      ) : (
        <EmptyState
          title="No private tasks"
          description="Create personal tasks that only you can access."
          action={
            <button
              type="button"
              className="rounded-lg bg-accent px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-600"
              onClick={() => openCreateTask({ scope: 'PRIVATE' })}
            >
              Create private task
            </button>
          }
        />
      )}
    </div>
  );
}
