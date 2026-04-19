import { X } from 'lucide-react';
import { useUiStore } from '../stores/uiStore';

export default function ConfirmModal() {
  const { confirm, closeConfirm } = useUiStore();
  if (!confirm) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-end justify-center bg-slate-900/50 p-0 sm:items-center sm:p-4">
      <div
        role="dialog"
        aria-modal="true"
        className="max-h-[92vh] w-full max-w-md overflow-y-auto rounded-t-2xl border border-bordercard bg-white p-6 shadow-xl sm:rounded-lg"
      >
        <div className="flex items-start justify-between gap-4">
          <h2 className="text-lg font-semibold text-slate-900">{confirm.title ?? 'Are you sure?'}</h2>
          <button
            type="button"
            className="rounded-lg p-1 text-slate-500 hover:bg-slate-100"
            onClick={closeConfirm}
            aria-label="Close"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <p className="mt-2 text-sm text-slate-600">{confirm.message}</p>
        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            className="rounded-lg border border-bordercard px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
            onClick={closeConfirm}
          >
            Cancel
          </button>
          <button
            type="button"
            className="rounded-lg bg-danger px-4 py-2 text-sm font-semibold text-white hover:bg-red-600"
            onClick={() => {
              confirm.onConfirm?.();
              closeConfirm();
            }}
          >
            {confirm.confirmLabel ?? 'Confirm'}
          </button>
        </div>
      </div>
    </div>
  );
}
