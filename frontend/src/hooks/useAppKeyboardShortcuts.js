import { useEffect } from 'react';
import { useUiStore } from '../stores/uiStore';

export function useAppKeyboardShortcuts() {
  const setCreateTaskOpen = useUiStore((s) => s.setCreateTaskOpen);
  const closeConfirm = useUiStore((s) => s.closeConfirm);
  const createTaskOpen = useUiStore((s) => s.createTaskOpen);
  const clearTaskDrawer = useUiStore((s) => s.clearTaskDrawer);

  useEffect(() => {
    const onKey = (e) => {
      const tag = e.target?.tagName;
      const inField =
        tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT' || e.target?.isContentEditable;

      if (e.key === 'Escape') {
        closeConfirm();
        if (createTaskOpen) setCreateTaskOpen(false);
        clearTaskDrawer();
        return;
      }

      if (inField) return;

      if ((e.key === 'c' || e.key === 'C') && !e.metaKey && !e.ctrlKey && !e.altKey) {
        e.preventDefault();
        setCreateTaskOpen(true);
        return;
      }

      if (e.key === '/' && !e.metaKey && !e.ctrlKey) {
        e.preventDefault();
        document.querySelector('[data-search-input]')?.focus();
      }
    };

    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [setCreateTaskOpen, closeConfirm, createTaskOpen, clearTaskDrawer]);
}
