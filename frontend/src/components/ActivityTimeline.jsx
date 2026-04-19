import { formatDistanceToNow } from 'date-fns';

const actionBorder = {
  CREATED: 'border-l-green-500',
  UPDATED: 'border-l-blue-500',
  STATUS_CHANGED: 'border-l-sky-500',
  ASSIGNED: 'border-l-amber-500',
  DELETED: 'border-l-red-500',
};

function formatStatus(s) {
  if (!s) return '';
  return String(s).replace(/_/g, ' ');
}

function describeChange(entry) {
  const name = entry.actorName || 'Someone';
  const oldV = entry.oldValue || {};
  const newV = entry.newValue || {};
  switch (entry.action) {
    case 'CREATED':
      return `${name} created this task`;
    case 'STATUS_CHANGED': {
      const from = formatStatus(oldV.status);
      const to = formatStatus(newV.status);
      return `${name} moved from ${from} → ${to}`;
    }
    case 'ASSIGNED': {
      const assignee = newV.assignedToName || 'Unassigned';
      return `${name} assigned this to ${assignee}`;
    }
    case 'DELETED':
      return `${name} deleted this task`;
    case 'UPDATED': {
      const descChanged =
        oldV.description !== undefined &&
        newV.description !== undefined &&
        String(oldV.description ?? '') !== String(newV.description ?? '');
      const titleChanged =
        oldV.title !== undefined &&
        newV.title !== undefined &&
        String(oldV.title) !== String(newV.title);
      if (descChanged && !titleChanged) {
        return `${name} updated the description`;
      }
      if (titleChanged) {
        return `${name} updated the title`;
      }
      return `${name} updated this task`;
    }
    default:
      return `${name} modified this task`;
  }
}

function Avatar({ name, url }) {
  const initial = name?.charAt(0)?.toUpperCase() ?? '?';
  if (url) {
    return (
      <img
        src={url}
        alt=""
        className="h-9 w-9 shrink-0 rounded-full border border-slate-200 object-cover"
      />
    );
  }
  return (
    <div
      className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-slate-200 text-xs font-semibold text-slate-700"
      aria-hidden
    >
      {initial}
    </div>
  );
}

export default function ActivityTimeline({ entries }) {
  if (!entries?.length) {
    return (
      <p className="text-sm text-slate-500" role="status">
        No activity yet.
      </p>
    );
  }

  return (
    <ol className="relative space-y-0 border-l border-slate-200 pl-6">
      {entries.map((entry) => {
        const border = actionBorder[entry.action] || 'border-l-slate-300';
        const isDeleted = entry.action === 'DELETED';
        return (
          <li key={entry.id} className="relative pb-8 last:pb-0">
            <span
              className={`absolute -left-[25px] mt-1.5 h-3 w-3 rounded-full border-2 border-white bg-white ring-2 ring-slate-200 ${
                entry.action === 'CREATED'
                  ? 'bg-emerald-500'
                  : entry.action === 'DELETED'
                    ? 'bg-red-500'
                    : 'bg-blue-500'
              }`}
              aria-hidden
            />
            <div
              className={`rounded-lg border border-slate-200 bg-white p-3 shadow-sm ${border} border-l-4 pl-4`}
            >
              <div className="flex gap-3">
                <Avatar name={entry.actorName} url={entry.actorAvatarUrl} />
                <div className="min-w-0 flex-1">
                  <p
                    className={`text-sm text-slate-800 ${isDeleted ? 'text-slate-500 line-through' : ''}`}
                  >
                    {describeChange(entry)}
                  </p>
                  <p className="mt-1 text-xs text-slate-500" title={new Date(entry.timestamp).toLocaleString()}>
                    {formatDistanceToNow(new Date(entry.timestamp), { addSuffix: true })}
                  </p>
                </div>
              </div>
            </div>
          </li>
        );
      })}
    </ol>
  );
}
