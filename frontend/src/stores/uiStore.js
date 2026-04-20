import { create } from 'zustand';

export const useUiStore = create((set) => ({
  confirm: null,
  openConfirm: (opts) => set({ confirm: opts }),
  closeConfirm: () => set({ confirm: null }),

  createTaskOpen: false,
  createTaskDraft: null,
  setCreateTaskOpen: (open) => set({ createTaskOpen: open }),
  openCreateTask: (draft = null) => set({ createTaskOpen: true, createTaskDraft: draft }),
  closeCreateTask: () => set({ createTaskOpen: false, createTaskDraft: null }),

  taskDrawerId: null,
  setTaskDrawerId: (id) => set({ taskDrawerId: id }),
  clearTaskDrawer: () => set({ taskDrawerId: null }),
}));
