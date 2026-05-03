import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { AuthGuard } from './index'
import * as useAuthModule from '../../hooks/useAuth'

vi.mock('../../hooks/useAuth')

const mockUseAuth = vi.mocked(useAuthModule.useAuth)

describe('AuthGuard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows a loading spinner while authenticating', () => {
    mockUseAuth.mockReturnValue({ user: undefined, isLoading: true, isAuthenticated: false })
    render(<AuthGuard><div>protected</div></AuthGuard>)
    expect(screen.queryByText('protected')).not.toBeInTheDocument()
    expect(document.querySelector('[role="progressbar"]')).toBeInTheDocument()
  })

  it('shows LoginPage when not authenticated', () => {
    mockUseAuth.mockReturnValue({ user: undefined, isLoading: false, isAuthenticated: false })
    render(<AuthGuard><div>protected</div></AuthGuard>)
    expect(screen.getByText(/login with discord/i)).toBeInTheDocument()
    expect(screen.queryByText('protected')).not.toBeInTheDocument()
  })

  it('renders children when authenticated', () => {
    mockUseAuth.mockReturnValue({
      user: { id: 1, discordId: '123', username: 'TestUser', avatarUrl: null },
      isLoading: false,
      isAuthenticated: true,
    })
    render(<AuthGuard><div>protected</div></AuthGuard>)
    expect(screen.getByText('protected')).toBeInTheDocument()
  })
})
