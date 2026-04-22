import { useMemo, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Send } from 'lucide-react';
import { useOutletContext } from 'react-router-dom';
import { api } from '../api/client';

export default function ChatPage() {
  const { user } = useOutletContext();
  const qc = useQueryClient();
  const [mode, setMode] = useState('ORG');
  const [peerId, setPeerId] = useState('');
  const [content, setContent] = useState('');

  const { data: users = [] } = useQuery({
    queryKey: ['org-users'],
    queryFn: async () => (await api.get('/users')).data,
  });

  const channelKey = useMemo(
    () => ['messages', mode, mode === 'DIRECT' ? peerId : 'org'],
    [mode, peerId],
  );

  const { data: messages = [] } = useQuery({
    queryKey: channelKey,
    queryFn: async () => {
      if (mode === 'DIRECT' && peerId) {
        const { data } = await api.get(`/messages/conversation/${peerId}`);
        return data;
      }
      const { data } = await api.get('/messages/org');
      return data;
    },
    refetchInterval: 4000,
  });

  const sendMutation = useMutation({
    mutationFn: async () => {
      const payload = { content };
      if (mode === 'DIRECT' && peerId) payload.receiverId = peerId;
      await api.post('/messages', payload);
    },
    onSuccess: () => {
      setContent('');
      qc.invalidateQueries({ queryKey: ['messages'] });
    },
  });

  return (
    <div className="grid gap-4 lg:grid-cols-[260px,1fr]">
      <aside className="rounded-xl border border-bordercard bg-white p-4 shadow-sm">
        <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">Channels</p>
        <button
          type="button"
          className={`mt-3 w-full rounded-lg px-3 py-2 text-left text-sm ${mode === 'ORG' ? 'bg-blue-50 text-blue-800' : 'hover:bg-slate-50'}`}
          onClick={() => {
            setMode('ORG');
            setPeerId('');
          }}
        >
          Organization Lounge
        </button>
        <p className="mt-4 text-xs font-semibold uppercase tracking-wide text-slate-500">
          Direct Messages
        </p>
        <div className="mt-2 max-h-[340px] space-y-1 overflow-auto">
          {users
            .filter((u) => u.id !== user?.id)
            .map((u) => (
              <button
                key={u.id}
                type="button"
                className={`w-full rounded-lg px-3 py-2 text-left text-sm ${mode === 'DIRECT' && peerId === u.id ? 'bg-blue-50 text-blue-800' : 'hover:bg-slate-50'}`}
                onClick={() => {
                  setMode('DIRECT');
                  setPeerId(u.id);
                }}
              >
                {u.fullName}
              </button>
            ))}
        </div>
      </aside>
      <section className="flex h-[70vh] flex-col rounded-xl border border-bordercard bg-white shadow-sm">
        <div className="border-b border-slate-100 px-4 py-3">
          <h1 className="text-lg font-semibold text-slate-900">
            {mode === 'ORG' ? 'Organization Lounge' : 'Direct Chat'}
          </h1>
        </div>
        <div className="flex-1 space-y-3 overflow-auto px-4 py-3">
          {messages
            .slice()
            .reverse()
            .map((m) => {
              const mine = m.senderId === user?.id;
              return (
                <div
                  key={m.id}
                  className={`max-w-[75%] rounded-2xl px-3 py-2 text-sm shadow ${mine ? 'ml-auto bg-blue-600 text-white' : 'bg-slate-100 text-slate-800'}`}
                >
                  <p className="text-[11px] opacity-80">{m.senderName}</p>
                  <p>{m.content}</p>
                </div>
              );
            })}
        </div>
        <div className="border-t border-slate-100 p-3">
          <div className="flex gap-2">
            <input
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={mode === 'ORG' ? 'Message your organization...' : 'Send a direct message...'}
              className="flex-1 rounded-lg border border-bordercard px-3 py-2 text-sm"
            />
            <button
              type="button"
              onClick={() => sendMutation.mutate()}
              disabled={!content.trim() || (mode === 'DIRECT' && !peerId)}
              className="inline-flex items-center gap-2 rounded-lg bg-accent px-4 py-2 text-sm font-semibold text-white disabled:opacity-60"
            >
              <Send className="h-4 w-4" />
              Send
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
