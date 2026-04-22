import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Users } from 'lucide-react';
import { Link, useOutletContext } from 'react-router-dom';
import toast from 'react-hot-toast';
import { api } from '../api/client';

function AvatarGroup({ memberCount }) {
  const visible = Math.min(memberCount || 0, 5);
  const extra = Math.max(0, (memberCount || 0) - visible);
  const list = Array.from({ length: visible }, (_, i) => i);
  return (
    <div className="flex items-center">
      {list.map((m) => (
        <span
          key={m}
          className="-ml-2 first:ml-0 inline-flex h-8 w-8 items-center justify-center rounded-full border-2 border-white bg-slate-200 text-[10px] font-semibold text-slate-700"
        >
          •
        </span>
      ))}
      {extra > 0 && <span className="ml-2 text-xs text-slate-500">+{extra} more</span>}
    </div>
  );
}

export default function TeamsPage() {
  const { user } = useOutletContext();
  const [inviteCode, setInviteCode] = useState('');
  const qc = useQueryClient();
  const { data: teams = [], isLoading } = useQuery({
    queryKey: ['teams'],
    queryFn: async () => (await api.get('/teams')).data,
  });
  const joinMutation = useMutation({
    mutationFn: (code) => api.post('/teams/join', { inviteCode: code }),
    onSuccess: () => {
      setInviteCode('');
      toast.success('Joined team');
      qc.invalidateQueries({ queryKey: ['teams'] });
    },
  });

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">My Teams</h1>
          <p className="text-sm text-slate-500">Teams you belong to.</p>
        </div>
        {user?.role === 'ADMIN' && (
          <Link to="/teams/manage" className="rounded-lg border border-bordercard px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50">
            Manage Teams
          </Link>
        )}
      </div>
      <div className="rounded-xl border border-bordercard bg-white p-4 shadow-sm">
        <p className="text-sm font-medium text-slate-800">Join a team by invite code</p>
        <div className="mt-2 flex flex-wrap gap-2">
          <input
            value={inviteCode}
            onChange={(e) => setInviteCode(e.target.value.toUpperCase())}
            placeholder="Enter invite code"
            className="rounded-lg border border-bordercard px-3 py-2 text-sm"
          />
          <button
            type="button"
            disabled={!inviteCode.trim() || joinMutation.isPending}
            onClick={() => joinMutation.mutate(inviteCode.trim())}
            className="rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white disabled:opacity-60"
          >
            Join Team
          </button>
        </div>
      </div>
      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">{[1, 2, 3].map((i) => <div key={i} className="h-40 animate-pulse rounded-xl bg-slate-200" />)}</div>
      ) : teams.length ? (
        <div className="grid gap-4 md:grid-cols-2">
          {teams.map((team) => (
            <article key={team.id} className="rounded-xl border border-bordercard bg-white p-4 shadow-sm">
              <h3 className="text-lg font-semibold text-slate-900">{team.name}</h3>
              <p className="mt-1 text-sm text-slate-500">{team.description || 'No description'}</p>
              <p className="mt-3 text-xs text-slate-500">{team.memberCount} members</p>
              <p className="mt-1 text-xs text-slate-500">Invite code: {team.inviteCode}</p>
              <div className="mt-2">
                <AvatarGroup memberCount={team.memberCount} />
              </div>
              <div className="mt-4">
                <Link to={`/teams/${team.id}`} className="rounded-lg bg-accent px-3 py-1.5 text-sm font-semibold text-white hover:bg-blue-600">
                  Open
                </Link>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <p className="rounded-lg border border-bordercard bg-white px-4 py-3 text-sm text-slate-600">
          <span className="inline-flex items-center gap-2"><Users className="h-4 w-4" />You are not a member of any team yet.</span>
        </p>
      )}
    </div>
  );
}
