import { apiClient } from '../../lib/apiClient'
import type { Collection } from '../../types'
import type { CreateCollectionRequest } from './types'

export const getCollections = (friendId: number): Promise<Collection[]> =>
  apiClient.get<Collection[]>(`/friends/${friendId}/collections`)

export const createCollection = (
  friendId: number,
  body: CreateCollectionRequest,
): Promise<Collection> => apiClient.post<Collection>(`/friends/${friendId}/collections`, body)

export const deleteCollection = (id: number): Promise<void> =>
  apiClient.delete(`/collections/${id}`)
