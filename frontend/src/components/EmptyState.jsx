function TasksIllustration() {
  return (
    <svg
      className="h-32 w-32 text-slate-200"
      viewBox="0 0 120 120"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden
    >
      <rect x="16" y="24" width="88" height="72" rx="8" stroke="currentColor" strokeWidth="2" />
      <path d="M32 44h56M32 56h40M32 68h48" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
      <circle cx="84" cy="84" r="16" className="fill-accent/20 stroke-accent" strokeWidth="2" />
      <path d="M78 84h12M84 78v12" stroke="currentColor" className="stroke-accent" strokeWidth="2" strokeLinecap="round" />
    </svg>
  );
}

export default function EmptyState({
  title = 'Nothing here',
  description,
  illustration,
  action,
}) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-bordercard bg-white px-4 py-12 text-center sm:py-16">
      {illustration ?? <TasksIllustration />}
      <p className="mt-6 text-sm font-semibold text-slate-800">{title}</p>
      {description && <p className="mt-2 max-w-md text-sm text-slate-500">{description}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
