import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Globe } from 'lucide-react';
import { useOutletContext } from 'react-router-dom';
import { api } from '../api/client';
import TaskCard from '../components/TaskCard';
import EmptyState from '../components/EmptyState';
import { useUiStore } from '../stores/uiStore';

export default function GlobalBoardPage() {
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const openCreateTask = useUiStore((s) => s.openCreateTask);
  const isAdmin = user?.role === 'ADMIN';

  const { data, isLoading } = useQuery({
    queryKey: ['global-tasks'],
    queryFn: async () => {
      const params = new URLSearchParams({ page: '0', size: '50' });
      const { data } = await api.get(`/tasks?${params}`);
      return (data.content || []).filter((t) => t.scope === 'GLOBAL');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => api.delete(`/tasks/${taskId}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['global-tasks'] }),
  });

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Global Board</h1>
          <p className="text-sm text-slate-500">Shared tasks visible to your full organization.</p>
        </div>
        {isAdmin && (
          <button
            type="button"
            onClick={() => openCreateTask({ scope: 'GLOBAL' })}
            className="rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-blue-600"
          >
            + Create Global Task
          </button>
        )}
      </div>
      {isLoading ? (
        <div className="grid gap-3 md:grid-cols-2">{[1, 2, 3].map((i) => <div key={i} className="h-36 animate-pulse rounded-lg bg-slate-200" />)}</div>
      ) : data?.length ? (
        <div className="grid gap-3 md:grid-cols-2">
          {data.map((task) => (
            <div key={task.id} className="rounded-lg border-l-4 border-l-blue-500">
              <TaskCard task={task} user={user} jwtRole={user?.role} onDelete={(t) => deleteMutation.mutate(t.id)} />
            </div>
          ))}
        </div>
      ) : (
        <EmptyState
          title="No global tasks"
          description="Use global tasks for org-wide work."
          action={
            isAdmin ? (
              <button
                type="button"
                className="rounded-lg bg-accent px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-600"
                onClick={() => openCreateTask({ scope: 'GLOBAL' })}
              >
                Create global task
              </button>
            ) : (
              <span className="inline-flex items-center gap-2 text-sm text-slate-500"><Globe className="h-4 w-4" />Read-only view for members</span>
            )
          }
        />
      )}
    </div>
  );
}
