import { apiClient } from '../../lib/apiClient'
import type { User } from '../../types'

export const searchUsers = (username: string): Promise<User[]> =>
  apiClient.get<User[]>(`/users/search?username=${encodeURIComponent(username)}`)
