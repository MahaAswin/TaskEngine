import { useEffect } from 'react';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '../stores/authStore';
import { useNotificationStore } from '../stores/useNotificationStore';

const TASK_EVENTS = new Set([
  'TASK_CREATED',
  'TASK_UPDATED',
  'TASK_DELETED',
  'TASK_STATUS_CHANGED',
]);

function makeId() {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function useSSENotifications(enabled = true) {
  const token = useAuthStore((s) => s.accessToken);
  const addNotification = useNotificationStore((s) => s.addNotification);
  const queryClient = useQueryClient();

  useEffect(() => {
    if (!enabled || !token) {
      return;
    }

    const controller = new AbortController();
    fetchEventSource('/api/notifications/stream', {
      method: 'GET',
      signal: controller.signal,
      headers: {
        Authorization: `Bearer ${token}`,
      },
      onmessage(ev) {
        if (!ev.data) {
          return;
        }
        try {
          const payload = JSON.parse(ev.data);
          const type = payload.type || ev.event || 'NOTIFICATION';
          addNotification({
            id: makeId(),
            type,
            message: payload.message || 'New notification',
            data: payload.data ?? null,
            timestamp: payload.timestamp ? new Date(payload.timestamp).toISOString() : new Date().toISOString(),
            read: false,
          });
          if (TASK_EVENTS.has(type)) {
            queryClient.invalidateQueries({ queryKey: ['tasks'] });
          }
        } catch {
          // ignore malformed events
        }
      },
      onerror() {
        return 5000;
      },
      openWhenHidden: true,
    });

    return () => controller.abort();
  }, [enabled, token, addNotification, queryClient]);
}
