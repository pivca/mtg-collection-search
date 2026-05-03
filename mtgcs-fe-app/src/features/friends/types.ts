export type { Friend } from '../../types'

export interface CreateFriendRequest {
  displayName: string
  discordId?: string
}

export interface UpdateFriendRequest {
  displayName: string
  discordId?: string
}
