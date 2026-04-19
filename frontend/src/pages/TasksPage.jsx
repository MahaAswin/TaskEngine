import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useNavigate, useOutletContext } from 'react-router-dom';
import { ArrowUpDown, Pencil, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { api } from '../api/client';
import StatusBadge from '../components/StatusBadge';
import StatusDropdown from '../components/StatusDropdown';
import PriorityBadge from '../components/PriorityBadge';
import EmptyState from '../components/EmptyState';
import { TableSkeleton } from '../components/skeletons';
import { useUiStore } from '../stores/uiStore';
import { useAuthStore } from '../stores/authStore';

function canMutateTask(user, task, jwtRole) {
  const admin = user?.role === 'ADMIN' || jwtRole === 'ADMIN';
  if (admin) return true;
  if (user?.id && task?.createdBy) return String(user.id) === String(task.createdBy);
  return false;
}

export default function TasksPage() {
  const navigate = useNavigate();
  const { user } = useOutletContext();
  const jwtRole = useAuthStore((s) => s.jwtRole);
  const setCreateTaskOpen = useUiStore((s) => s.setCreateTaskOpen);
  const setTaskDrawerId = useUiStore((s) => s.setTaskDrawerId);
  const openConfirm = useUiStore((s) => s.openConfirm);
  const qc = useQueryClient();

  const [sort, setSort] = useState('createdAt,desc');
  const [page, setPage] = useState(0);
  const [status, setStatus] = useState('');
  const [search, setSearch] = useState('');
  const size = 10;

  const { data: pageData, isLoading } = useQuery({
    queryKey: ['tasks', 'table', page, size, sort, status, search],
    queryFn: async () => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort,
      });
      if (status) params.set('status', status);
      if (search.trim()) params.set('search', search.trim());
      const { data } = await api.get(`/tasks?${params}`);
      return data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => api.delete(`/tasks/${taskId}`),
    onSuccess: (_, taskId) => {
      qc.invalidateQueries({ queryKey: ['tasks'] });
      qc.invalidateQueries({ queryKey: ['task-stats'] });
      toast.custom(
        (t) => (
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
        ),
        { duration: 8000 },
      );
    },
  });

  const toggleSort = (field) => {
    const [cur, dir] = sort.split(',');
    const nextDir = cur === field && dir === 'desc' ? 'asc' : 'desc';
    setSort(`${field},${nextDir}`);
  };

  const rows = pageData?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Tasks</h1>
          <p className="text-sm text-slate-500">All tasks in your organization</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <input
            data-search-input
            type="search"
            placeholder="Search tasks…"
            aria-label="Search tasks"
            className="min-w-[200px] rounded-lg border border-bordercard bg-white px-3 py-2 text-sm"
            value={search}
            onChange={(e) => {
              setPage(0);
              setSearch(e.target.value);
            }}
          />
          <select
            className="rounded-lg border border-bordercard bg-white px-3 py-2 text-sm"
            value={status}
            onChange={(e) => {
              setPage(0);
              setStatus(e.target.value);
            }}
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
      {isLoading ? (
        <TableSkeleton rows={8} cols={7} />
      ) : (
        <div className="overflow-hidden rounded-lg border border-bordercard bg-white shadow-sm">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">
                  <button
                    type="button"
                    className="inline-flex items-center gap-1 hover:text-accent"
                    onClick={() => toggleSort('title')}
                    aria-label="Sort by title"
                  >
                    Title
                    <ArrowUpDown className="h-3.5 w-3.5" aria-hidden />
                  </button>
                </th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Status</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Priority</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Assignee</th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">
                  <button
                    type="button"
                    className="inline-flex items-center gap-1 hover:text-accent"
                    onClick={() => toggleSort('dueDate')}
                    aria-label="Sort by due date"
                  >
                    Due
                    <ArrowUpDown className="h-3.5 w-3.5" aria-hidden />
                  </button>
                </th>
                <th className="px-4 py-3 text-left font-semibold text-slate-700">Created by</th>
                <th className="px-4 py-3 text-right font-semibold text-slate-700">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {rows.map((t) => {
                const can = canMutateTask(user, t, jwtRole);
                return (
                  <tr
                    key={t.id}
                    className="cursor-pointer hover:bg-slate-50"
                    onClick={() => setTaskDrawerId(t.id)}
                  >
                    <td className="px-4 py-3 font-medium text-slate-900">{t.title}</td>
                    <td className="px-4 py-3" onClick={(e) => e.stopPropagation()}>
                      <StatusDropdown taskId={t.id} status={t.status} disabled={t.deleted || !can} />
                    </td>
                    <td className="px-4 py-3">
                      <PriorityBadge priority={t.priority} />
                    </td>
                    <td className="px-4 py-3 text-slate-600">{t.assignedToName ?? '—'}</td>
                    <td className="px-4 py-3 font-mono text-xs text-slate-600">{t.dueDate ?? '—'}</td>
                    <td className="px-4 py-3 text-slate-600">{t.createdByName}</td>
                    <td className="px-4 py-3 text-right" onClick={(e) => e.stopPropagation()}>
                      {can && (
                        <div className="flex justify-end gap-1">
                          <button
                            type="button"
                            className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 hover:text-accent"
                            title="Edit"
                            aria-label="Edit task"
                            onClick={() => navigate(`/tasks/${t.id}`)}
                          >
                            <Pencil className="h-4 w-4" />
                          </button>
                          <button
                            type="button"
                            className="rounded-lg p-2 text-slate-600 hover:bg-red-50 hover:text-danger"
                            title="Delete"
                            aria-label="Delete task"
                            onClick={() =>
                              openConfirm({
                                title: 'Delete task',
                                message: `Delete “${t.title}”?`,
                                confirmLabel: 'Delete',
                                onConfirm: () => deleteMutation.mutate(t.id),
                              })
                            }
                          >
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          {!rows.length && (
            <div className="p-6">
              <EmptyState
                title="No tasks yet"
                description="Create your first task to get started with your team."
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
            </div>
          )}
        </div>
      )}
      {pageData && pageData.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <button
            type="button"
            disabled={page <= 0}
            className="rounded-lg border border-bordercard px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            aria-label="Previous page"
          >
            Previous
          </button>
          <span className="text-slate-600">
            Page {page + 1} of {pageData.totalPages}
          </span>
          <button
            type="button"
            disabled={page >= pageData.totalPages - 1}
            className="rounded-lg border border-bordercard px-3 py-1 disabled:opacity-50"
            onClick={() => setPage((p) => p + 1)}
            aria-label="Next page"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
