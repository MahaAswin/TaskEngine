import { useMemo, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useOutletContext, useParams } from 'react-router-dom';
import { api } from '../api/client';
import TaskKanbanBoard from '../components/TaskKanbanBoard';
import { useUiStore } from '../stores/uiStore';

function roleBadge(role) {
  return role === 'TEAM_LEADER'
    ? 'inline-flex rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-800'
    : 'inline-flex rounded-full bg-slate-200 px-2 py-0.5 text-xs font-semibold text-slate-700';
}

export default function TeamDetailPage() {
  const { teamId } = useParams();
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const openCreateTask = useUiStore((s) => s.openCreateTask);
  const [newMemberId, setNewMemberId] = useState('');

  const { data: team, isLoading } = useQuery({
    queryKey: ['team', teamId],
    queryFn: async () => (await api.get(`/teams/${teamId}`)).data,
  });

  const { data: tasks = [] } = useQuery({
    queryKey: ['team-tasks', teamId],
    queryFn: async () => {
      const params = new URLSearchParams({ page: '0', size: '100', teamId });
      const { data } = await api.get(`/tasks?${params}`);
      return data.content || [];
    },
  });

  const { data: activity = [] } = useQuery({
    queryKey: ['team-activity', teamId],
    queryFn: async () => (await api.get(`/teams/${teamId}/activity`)).data,
  });

  const isAdmin = user?.role === 'ADMIN';
  const isLeader = isAdmin || team?.myRole === 'TEAM_LEADER';

  const members = useMemo(() => team?.members || [], [team]);

  const { data: candidates = [] } = useQuery({
    queryKey: ['team-candidates', teamId],
    queryFn: async () => {
      const { data } = await api.get(`/teams/${teamId}/candidates`);
      return data;
    },
    enabled: isLeader,
  });

  if (isLoading) {
    return <div className="h-40 animate-pulse rounded-xl bg-slate-200" />;
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
      <section className="space-y-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h1 className="text-2xl font-semibold text-slate-900">{team?.name}</h1>
            <p className="text-sm text-slate-500">{team?.description || 'No description'}</p>
          </div>
          {isLeader && (
            <button
              type="button"
              className="rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white hover:bg-blue-600"
              onClick={() => openCreateTask({ scope: 'TEAM', teamId })}
            >
              + Assign Task
            </button>
          )}
        </div>
        <TaskKanbanBoard teamId={teamId} tasks={tasks} user={user} myRole={team?.myRole} />
      </section>

      <aside className="space-y-5">
        <section className="rounded-xl border border-bordercard bg-white p-4">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Team Members</h2>
          <div className="mt-3 space-y-2">
            {members.map((m) => (
              <div key={m.userId} className="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
                <div>
                  <p className="text-sm font-medium text-slate-800">{m.fullName}</p>
                </div>
                <span className={roleBadge(m.role)}>{m.role}</span>
              </div>
            ))}
          </div>
          {isLeader && (
            <div className="mt-4 space-y-2">
              <select
                className="w-full rounded-lg border border-bordercard px-3 py-2 text-sm font-medium text-slate-700"
                value={newMemberId}
                onChange={(e) => setNewMemberId(e.target.value)}
              >
                <option value="">Select a user to add</option>
                {candidates.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.fullName} ({u.email})
                  </option>
                ))}
              </select>
              <button
                type="button"
                className="w-full rounded-lg border border-bordercard px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                disabled={!newMemberId}
                onClick={async () => {
                  if (!newMemberId) return;
                  await api.post(`/teams/${teamId}/members`, { userId: newMemberId }, { skipGlobalErrorToast: true });
                  setNewMemberId('');
                  qc.invalidateQueries({ queryKey: ['team', teamId] });
                  qc.invalidateQueries({ queryKey: ['team-candidates', teamId] });
                  qc.invalidateQueries({ queryKey: ['team-activity', teamId] });
                }}
              >
                Add Member
              </button>
            </div>
          )}
        </section>

        <section className="rounded-xl border border-bordercard bg-white p-4">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">Team Activity</h2>
          <div className="mt-3 space-y-2">
            {activity.slice(0, 8).map((entry) => (
              <div key={entry.id} className="rounded-lg border border-slate-100 px-3 py-2 text-xs text-slate-700">
                <p className="font-medium">{entry.action.replaceAll('_', ' ')}</p>
                <p className="text-slate-500">
                  {entry.taskTitle ? `Task: ${entry.taskTitle}` : entry.affectedUserName ? `User: ${entry.affectedUserName}` : entry.actorName}
                </p>
              </div>
            ))}
          </div>
        </section>
      </aside>
    </div>
  );
}
