import { apiClient } from '../../lib/apiClient'
import type { Friend } from '../../types'
import type { CreateFriendRequest, UpdateFriendRequest } from './types'

export const getFriends = (): Promise<Friend[]> => apiClient.get<Friend[]>('/friends')

export const createFriend = (body: CreateFriendRequest): Promise<Friend> =>
  apiClient.post<Friend>('/friends', body)

export const updateFriend = (id: number, body: UpdateFriendRequest): Promise<Friend> =>
  apiClient.put<Friend>(`/friends/${id}`, body)

export const deleteFriend = (id: number): Promise<void> => apiClient.delete(`/friends/${id}`)
