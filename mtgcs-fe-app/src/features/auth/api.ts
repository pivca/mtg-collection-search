import { apiClient } from '../../lib/apiClient'
import type { User } from './types'

export const getMe = (): Promise<User> => apiClient.get<User>('/users/me')
