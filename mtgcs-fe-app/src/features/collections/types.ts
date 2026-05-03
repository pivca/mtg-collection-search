export type { Collection, SourceType } from '../../types'
import type { SourceType } from '../../types'

export interface CreateCollectionRequest {
  sourceType: SourceType
  sourceUrl: string
}

export const SOURCE_URL_PATTERN = /^https:\/\/(www\.)?(deckbox\.org|moxfield\.com)\/.+$/

export const parseSourceType = (url: string): SourceType | null => {
  if (/moxfield\.com/i.test(url)) return 'MOXFIELD'
  if (/deckbox\.org/i.test(url)) return 'DECKBOX'
  return null
}
