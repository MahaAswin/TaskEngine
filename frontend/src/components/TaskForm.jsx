import { useEffect, useState } from 'react';
import { Plus, Save } from 'lucide-react';

const empty = {
  title: '',
  description: '',
  status: 'TODO',
  priority: 'MEDIUM',
  dueDate: '',
  assignedToId: '',
};

function taskToForm(task) {
  if (!task) return { ...empty };
  return {
    title: task.title || '',
    description: task.description || '',
    status: task.status || 'TODO',
    priority: task.priority || 'MEDIUM',
    dueDate: task.dueDate || '',
    assignedToId: task.assignedTo || '',
  };
}

export default function TaskForm({ initialTask, onSubmit, onCancel }) {
  const [form, setForm] = useState(() => taskToForm(initialTask));

  useEffect(() => {
    setForm(taskToForm(initialTask));
  }, [initialTask]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const payload = {
      title: form.title,
      description: form.description || '',
      status: form.status,
      priority: form.priority,
      dueDate: form.dueDate ? form.dueDate : null,
      assignedToId: form.assignedToId?.trim() ? form.assignedToId.trim() : null,
    };
    await onSubmit(payload);
    if (!initialTask) {
      setForm({ ...empty });
    }
  };

  const input =
    'w-full rounded-lg border border-bordercard bg-white px-3 py-2.5 text-sm shadow-sm transition hover:border-slate-300 focus:border-accent focus:ring-2 focus:ring-accent/20';

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-lg border border-bordercard bg-white p-4 shadow-sm sm:p-6"
    >
      <div className="mb-4 flex items-center gap-2">
        {initialTask ? (
          <Save className="h-5 w-5 text-accent" />
        ) : (
          <Plus className="h-5 w-5 text-accent" />
        )}
        <h3 className="text-base font-semibold text-slate-900">
          {initialTask ? 'Edit task' : 'Create task'}
        </h3>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium text-slate-700">Title</label>
          <input
            className={input}
            placeholder="Short, actionable title"
            value={form.title}
            onChange={(e) => setForm((p) => ({ ...p, title: e.target.value }))}
            required
          />
        </div>
        <div className="sm:col-span-2">
          <label className="mb-1 block text-sm font-medium text-slate-700">Description</label>
          <textarea
            className={`${input} min-h-[96px] resize-y`}
            placeholder="Context, acceptance criteria, links..."
            value={form.description}
            onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
            rows={4}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Status</label>
          <select
            className={input}
            value={form.status}
            onChange={(e) => setForm((p) => ({ ...p, status: e.target.value }))}
          >
            <option value="TODO">To do</option>
            <option value="IN_PROGRESS">In progress</option>
            <option value="IN_REVIEW">In review</option>
            <option value="DONE">Done</option>
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Priority</label>
          <select
            className={input}
            value={form.priority}
            onChange={(e) => setForm((p) => ({ ...p, priority: e.target.value }))}
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Due date</label>
          <input
            type="date"
            className={input}
            value={form.dueDate}
            onChange={(e) => setForm((p) => ({ ...p, dueDate: e.target.value }))}
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Assignee user ID</label>
          <input
            className={`${input} font-mono text-xs sm:text-sm`}
            placeholder="UUID (optional)"
            value={form.assignedToId}
            onChange={(e) => setForm((p) => ({ ...p, assignedToId: e.target.value }))}
          />
        </div>
      </div>
      <div className="mt-6 flex flex-wrap gap-2">
        <button
          type="submit"
          className="inline-flex items-center justify-center rounded-lg bg-accent px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-600 active:scale-[0.99]"
        >
          {initialTask ? 'Save changes' : 'Create task'}
        </button>
        {initialTask && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-lg border border-bordercard bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50 active:bg-slate-100"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
