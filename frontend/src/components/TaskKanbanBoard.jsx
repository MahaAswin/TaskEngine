import { useMemo, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { DndContext, PointerSensor, useSensor, useSensors, closestCorners } from '@dnd-kit/core';
import { SortableContext, useSortable, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripVertical, Plus } from 'lucide-react';
import { format, isPast, isToday, parseISO } from 'date-fns';
import toast from 'react-hot-toast';
import { api } from '../api/client';
import { useUiStore } from '../stores/uiStore';

const COLUMNS = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];
const LABELS = { TODO: 'To Do', IN_PROGRESS: 'In Progress', IN_REVIEW: 'In Review', DONE: 'Done' };
const BORDER = { TODO: 'border-t-slate-400', IN_PROGRESS: 'border-t-blue-500', IN_REVIEW: 'border-t-amber-500', DONE: 'border-t-emerald-500' };

function dueTone(date) {
  if (!date) return 'text-slate-500';
  const d = typeof date === 'string' ? parseISO(date) : date;
  if (isToday(d)) return 'text-amber-600';
  if (isPast(d)) return 'text-red-600';
  return 'text-slate-500';
}

function PriorityDot({ priority }) {
  const cls =
    priority === 'CRITICAL'
      ? 'bg-red-500 animate-pulse'
      : priority === 'HIGH'
        ? 'bg-orange-500'
        : priority === 'MEDIUM'
          ? 'bg-blue-500'
          : 'bg-slate-400';
  return <span className={`inline-block h-2.5 w-2.5 rounded-full ${cls}`} />;
}

function initials(name) {
  if (!name) return '?';
  return name
    .split(' ')
    .slice(0, 2)
    .map((n) => n.charAt(0).toUpperCase())
    .join('');
}

function Card({ task, canDrag }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id: task.id,
    disabled: !canDrag,
  });
  return (
    <article
      ref={setNodeRef}
      style={{ transform: CSS.Transform.toString(transform), transition }}
      className={`rounded-lg border border-bordercard bg-white p-3 shadow-sm ${isDragging ? 'opacity-70' : ''}`}
    >
      <div className="flex items-start justify-between gap-2">
        <h4 className="text-sm font-semibold text-slate-900">{task.title}</h4>
        <button
          type="button"
          className={`rounded p-1 ${canDrag ? 'text-slate-500 hover:bg-slate-100' : 'text-slate-300'}`}
          {...attributes}
          {...listeners}
          disabled={!canDrag}
          aria-label="Drag task"
        >
          <GripVertical className="h-4 w-4" />
        </button>
      </div>
      <div className="mt-2 flex items-center gap-2 text-xs text-slate-600">
        <PriorityDot priority={task.priority} />
        <span>{task.priority}</span>
      </div>
      <div className="mt-3 flex items-center justify-between text-xs">
        <div title={task.assignedToName || 'Unassigned'} className="inline-flex items-center gap-2">
          <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-slate-200 text-[10px] font-semibold text-slate-700">
            {initials(task.assignedToName)}
          </span>
          <span className="hidden text-slate-600 sm:inline">{task.assignedToName || 'Unassigned'}</span>
        </div>
        <span className={`font-mono ${dueTone(task.dueDate)}`}>
          {task.dueDate ? format(parseISO(task.dueDate), 'yyyy-MM-dd') : 'No due date'}
        </span>
      </div>
    </article>
  );
}

export default function TaskKanbanBoard({ teamId, tasks, user, myRole }) {
  const qc = useQueryClient();
  const openCreateTask = useUiStore((s) => s.openCreateTask);
  const [mobileColumn, setMobileColumn] = useState('TODO');
  const isAdmin = user?.role === 'ADMIN';
  const isLeader = isAdmin || myRole === 'TEAM_LEADER';
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 8 } }));

  const byStatus = useMemo(() => {
    const out = { TODO: [], IN_PROGRESS: [], IN_REVIEW: [], DONE: [] };
    for (const task of tasks || []) out[task.status]?.push(task);
    return out;
  }, [tasks]);

  const moveMutation = useMutation({
    onMutate: async (vars) => {
      const queryKey = ['team-tasks', teamId];
      await qc.cancelQueries({ queryKey });
      const previous = qc.getQueryData(queryKey);
      qc.setQueryData(queryKey, (old = []) =>
        old.map((t) => (t.id === vars.id ? { ...t, status: vars.status } : t)),
      );
      return { previous, queryKey };
    },
    mutationFn: ({ id, status }) => api.patch(`/tasks/${id}/status`, { status }, { skipGlobalErrorToast: true }),
    onSuccess: (_, vars) => {
      toast.success(`Task moved to ${LABELS[vars.status]}`);
    },
    onError: (_err, vars, ctx) => {
      if (ctx?.previous) qc.setQueryData(ctx.queryKey, ctx.previous);
      toast.error('Could not move task');
    },
  });

  const canMoveTask = (task) => isLeader || String(task.assignedTo || '') === String(user?.id || '');

  const handleDragEnd = ({ active, over }) => {
    if (!over) return;
    const task = tasks.find((t) => t.id === active.id);
    if (!task || !canMoveTask(task)) return;
    const nextStatus =
      COLUMNS.includes(over.id) ? over.id : tasks.find((t) => t.id === over.id)?.status;
    if (!COLUMNS.includes(nextStatus) || task.status === nextStatus) return;
    moveMutation.mutate({ id: task.id, status: nextStatus });
  };

  const columnUI = (status) => (
    <section key={status} className={`rounded-xl border border-bordercard bg-slate-50/60`}>
      <header className={`border-t-4 ${BORDER[status]} rounded-t-xl px-3 py-2`}>
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-slate-800">{LABELS[status]}</h3>
          <span className="rounded-full bg-white px-2 py-0.5 text-xs text-slate-600">{byStatus[status].length}</span>
        </div>
      </header>
      <div className="max-h-[60vh] space-y-2 overflow-y-auto px-3 pb-3">
        {isLeader && (
          <button
            type="button"
            className="mt-1 inline-flex items-center gap-1 rounded-md border border-dashed border-slate-300 px-2 py-1 text-xs text-slate-600 hover:bg-white"
            onClick={() =>
              openCreateTask({
                status,
                scope: 'TEAM',
                teamId,
              })
            }
          >
            <Plus className="h-3.5 w-3.5" />
            Add
          </button>
        )}
        <SortableContext items={byStatus[status].map((t) => t.id)} strategy={verticalListSortingStrategy}>
          {byStatus[status].map((task) => (
            <Card key={task.id} task={task} canDrag={canMoveTask(task)} />
          ))}
        </SortableContext>
      </div>
    </section>
  );

  return (
    <div className="space-y-4">
      <div className="hidden gap-4 lg:grid lg:grid-cols-4">
        <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
          {COLUMNS.map(columnUI)}
        </DndContext>
      </div>
      <div className="hidden gap-4 md:grid md:grid-cols-2 lg:hidden">
        <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
          {COLUMNS.map(columnUI)}
        </DndContext>
      </div>
      <div className="md:hidden">
        <div className="mb-3 flex gap-2 overflow-x-auto">
          {COLUMNS.map((col) => (
            <button
              key={col}
              type="button"
              className={`rounded-full px-3 py-1 text-xs font-medium ${mobileColumn === col ? 'bg-accent text-white' : 'bg-slate-100 text-slate-700'}`}
              onClick={() => setMobileColumn(col)}
            >
              {LABELS[col]}
            </button>
          ))}
        </div>
        <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
          {columnUI(mobileColumn)}
        </DndContext>
      </div>
    </div>
  );
}
