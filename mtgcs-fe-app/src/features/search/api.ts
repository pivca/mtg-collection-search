import { apiClient } from '../../lib/apiClient'
import type { SearchResponse } from '../../types'

export const searchCards = (cardNames: string[]): Promise<SearchResponse> =>
  apiClient.post<SearchResponse>('/search', { cardNames })
