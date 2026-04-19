import { useQuery } from '@tanstack/react-query';
import { api } from '../api/client';

export function useCurrentUser() {
  return useQuery({
    queryKey: ['me'],
    queryFn: async () => {
      const { data } = await api.get('/users/me');
      return data;
    },
    retry: false,
  });
}
