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
}

export interface CardSearchResult {
  cardName: string
  matches: CardMatch[]
}
