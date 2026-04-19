function pulse(cls = '') {
  return `animate-pulse rounded-md bg-slate-200/90 ${cls}`;
}

export function Skeleton({ className = '' }) {
  return <div className={pulse(className)} aria-hidden />;
}

export function AppShellSkeleton() {
  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      <aside className="hidden w-64 border-r border-slate-200 bg-sidebar p-6 md:block">
        <Skeleton className="h-8 w-24" />
        <div className="mt-8 space-y-3">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-10 w-full" />
        </div>
      </aside>
      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <div className="border-b border-bordercard bg-white px-4 py-4">
          <div className="flex justify-between">
            <div className="space-y-2">
              <Skeleton className="h-3 w-24" />
              <Skeleton className="h-5 w-40" />
            </div>
            <Skeleton className="h-9 w-9 rounded-full" />
          </div>
        </div>
        <main className="flex-1 space-y-6 p-4 md:p-6 lg:p-8">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {[1, 2, 3, 4].map((i) => (
              <Skeleton key={i} className="h-24 w-full" />
            ))}
          </div>
          <Skeleton className="h-40 w-full" />
        </main>
      </div>
    </div>
  );
}

export function TableSkeleton({ rows = 6, cols = 7 }) {
  return (
    <div className="overflow-hidden rounded-lg border border-bordercard bg-white">
      <div className="grid gap-px bg-slate-100 p-3" style={{ gridTemplateColumns: `repeat(${cols}, minmax(0,1fr))` }}>
        {Array.from({ length: cols }).map((_, i) => (
          <Skeleton key={`h-${i}`} className="h-4 w-full" />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, r) => (
        <div
          key={r}
          className="grid gap-px border-t border-slate-100 p-3"
          style={{ gridTemplateColumns: `repeat(${cols}, minmax(0,1fr))` }}
        >
          {Array.from({ length: cols }).map((_, c) => (
            <Skeleton key={c} className="h-5 w-full" />
          ))}
        </div>
      ))}
    </div>
  );
}

export function TaskDetailSkeleton() {
  return (
    <div className="grid gap-8 lg:grid-cols-[1fr_340px]">
      <div className="space-y-4">
        <Skeleton className="h-8 w-2/3 max-w-md" />
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-10 w-32" />
      </div>
      <div className="space-y-3">
        <Skeleton className="h-5 w-24" />
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} className="h-20 w-full" />
        ))}
      </div>
    </div>
  );
}

export function StatCardsSkeleton({ count = 4 }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className="h-24 w-full" />
      ))}
    </div>
  );
}
