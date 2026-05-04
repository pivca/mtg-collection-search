export type SourceType = 'DECKBOX' | 'MOXFIELD'

export interface User {
  id: number
  discordId: string
  username: string
  avatarUrl: string | null
}

export interface Friend {
  id: number
  displayName: string
  discordId: string | null
  createdAt: string
}

export interface Collection {
  id: number
  friendId: number
  friendDisplayName: string
  sourceType: SourceType
  sourceUrl: string
  addedAt: string
}

export interface CardMatch {
  friendId: number
  friendName: string
  collectionId: number
  sourceUrl: string
  sourceType: SourceType
  quantity: number
  price: string | null
  cardPageUrl: string | null
  edition: string | null
  language: string | null
  foil: boolean
  setCode: string | null
}

export interface CardSearchResult {
  cardName: string
  matches: CardMatch[]
}

export type ActionType =
  | 'CARD_SEARCH'
  | 'FRIEND_CREATED'
  | 'FRIEND_UPDATED'
  | 'FRIEND_DELETED'
  | 'COLLECTION_ADDED'
  | 'COLLECTION_DELETED'

export interface HistoryEntry {
  id: number
  actionType: ActionType
  summary: string
  payload: string | null
  createdAt: string
}
