import { useQuery } from '@tanstack/react-query'
import { getMe } from '../features/auth/api'
import type { User } from '../types'

export const useAuth = () => {
  const { data: user, isLoading, error } = useQuery<User>({
    queryKey: ['auth', 'me'],
    queryFn: getMe,
    retry: false,
    staleTime: 5 * 60 * 1000,
  })

  return {
    user,
    isLoading,
    isAuthenticated: !!user && !error,
  }
}
