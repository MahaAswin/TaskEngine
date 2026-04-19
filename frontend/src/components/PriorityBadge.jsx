const dot = {
  LOW: 'bg-slate-400',
  MEDIUM: 'bg-accent',
  HIGH: 'bg-warning',
  CRITICAL: 'bg-danger',
};

export default function PriorityBadge({ priority }) {
  const d = dot[priority] || dot.MEDIUM;
  const isCritical = priority === 'CRITICAL';
  return (
    <span
      className="inline-flex items-center gap-1.5 text-xs font-medium text-slate-700"
      title={isCritical ? 'Critical priority' : undefined}
    >
      <span
        className={`h-2 w-2 rounded-full ${d} ${isCritical ? 'animate-pulse' : ''}`}
        aria-hidden
      />
      <span className="sr-only">{isCritical ? 'Critical priority: ' : ''}</span>
      {priority}
    </span>
  );
}
