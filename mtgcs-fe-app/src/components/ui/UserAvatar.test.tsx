import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import UserAvatar from './UserAvatar'
import * as useAuthModule from '../../hooks/useAuth'

vi.mock('../../hooks/useAuth')

const mockUseAuth = vi.mocked(useAuthModule.useAuth)

describe('UserAvatar', () => {
  it('renders nothing when user is not loaded', () => {
    mockUseAuth.mockReturnValue({ user: undefined, isLoading: true, isAuthenticated: false })
    const { container } = render(<UserAvatar />)
    expect(container).toBeEmptyDOMElement()
  })

  it('renders the username when authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: { id: 1, discordId: '123', username: 'DiscordUser', avatarUrl: null },
      isLoading: false,
      isAuthenticated: true,
    })
    render(<UserAvatar />)
    expect(screen.getByText('DiscordUser')).toBeInTheDocument()
  })
})
