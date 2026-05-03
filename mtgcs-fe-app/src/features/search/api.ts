import { apiClient } from '../../lib/apiClient'
import type { CardSearchResult } from '../../types'

export const searchCards = (cardNames: string[]): Promise<CardSearchResult[]> =>
  apiClient.post<CardSearchResult[]>('/search', { cardNames })
