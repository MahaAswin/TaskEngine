import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Navigate, useOutletContext } from 'react-router-dom';
import { api } from '../api/client';
import { TableSkeleton } from '../components/skeletons';

export default function TeamPage() {
  const { user } = useOutletContext();
  const qc = useQueryClient();

  const { data: members, isLoading } = useQuery({
    queryKey: ['org-members'],
    queryFn: async () => {
      const { data } = await api.get('/org/members');
      return data;
    },
    enabled: user?.role === 'ADMIN',
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }) => api.patch(`/users/${id}/role`, { role }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['org-members'] }),
  });

  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" replace />;
  }

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Manage Teams</h1>
          <p className="text-sm text-slate-500">Organization member administration</p>
        </div>
        <TableSkeleton rows={5} cols={4} />
      </div>
    );
  }

  const list = members ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">Manage Teams</h1>
        <p className="text-sm text-slate-500">Organization member administration</p>
      </div>
      {list.length <= 1 && (
        <p className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-950">
          Your team is just you for now. Invite members to collaborate.
        </p>
      )}
      <div className="overflow-hidden rounded-lg border border-bordercard bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th className="px-4 py-3 text-left font-semibold">Name</th>
              <th className="px-4 py-3 text-left font-semibold">Email</th>
              <th className="px-4 py-3 text-left font-semibold">Role</th>
              <th className="px-4 py-3 text-left font-semibold">Tasks created</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {list.map((m) => (
              <tr key={m.id}>
                <td className="px-4 py-3 font-medium text-slate-900">{m.fullName}</td>
                <td className="px-4 py-3 text-slate-600">{m.email}</td>
                <td className="px-4 py-3">
                  <select
                    className="rounded-lg border border-bordercard px-2 py-1 text-sm"
                    value={m.role}
                    disabled={m.id === user.id}
                    onChange={(e) =>
                      roleMutation.mutate({ id: m.id, role: e.target.value })
                    }
                  >
                    <option value="ADMIN">ADMIN</option>
                    <option value="MEMBER">MEMBER</option>
                  </select>
                </td>
                <td className="px-4 py-3 text-slate-600">{m.taskCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
