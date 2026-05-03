import { apiClient } from '../../lib/apiClient'
import type { DiscordUser } from './types'

export const lookupDiscordUser = (discordId: string): Promise<DiscordUser> =>
  apiClient.get<DiscordUser>(`/discord/users/${encodeURIComponent(discordId)}`)
