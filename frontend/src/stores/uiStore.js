import { create } from 'zustand';

export const useUiStore = create((set) => ({
  confirm: null,
  openConfirm: (opts) => set({ confirm: opts }),
  closeConfirm: () => set({ confirm: null }),

  createTaskOpen: false,
  setCreateTaskOpen: (open) => set({ createTaskOpen: open }),

  taskDrawerId: null,
  setTaskDrawerId: (id) => set({ taskDrawerId: id }),
  clearTaskDrawer: () => set({ taskDrawerId: null }),
}));
