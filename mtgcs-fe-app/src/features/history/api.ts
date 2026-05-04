import type { HistoryEntry } from '../../types'
import { apiClient } from '../../lib/apiClient'

export const fetchHistory = (): Promise<HistoryEntry[]> => apiClient.get<HistoryEntry[]>('/history')
