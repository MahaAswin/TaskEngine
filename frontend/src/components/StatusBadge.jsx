const styles = {
  TODO: 'bg-slate-100 text-slate-700 ring-1 ring-slate-200',
  IN_PROGRESS: 'bg-blue-50 text-blue-800 ring-1 ring-blue-200',
  IN_REVIEW: 'bg-amber-50 text-amber-900 ring-1 ring-amber-200',
  DONE: 'bg-emerald-50 text-emerald-800 ring-1 ring-emerald-200',
};

export default function StatusBadge({ status }) {
  const cls = styles[status] || styles.TODO;
  const label = status?.replace(/_/g, ' ') ?? '';
  return (
    <span className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium capitalize ${cls}`}>
      {label}
    </span>
  );
}
