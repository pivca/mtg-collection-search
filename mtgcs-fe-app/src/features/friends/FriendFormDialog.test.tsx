import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import FriendFormDialog from './FriendFormDialog'
import * as useFriendsModule from '../../hooks/useFriends'

vi.mock('../../hooks/useFriends')

const mockUseFriends = vi.mocked(useFriendsModule.useFriends)
const mockCreate = { mutate: vi.fn(), isPending: false }
const mockUpdate = { mutate: vi.fn(), isPending: false }

beforeEach(() => {
  vi.clearAllMocks()
  mockUseFriends.mockReturnValue({
    data: [],
    isLoading: false,
    create: mockCreate,
    update: mockUpdate,
    remove: { mutate: vi.fn() },
  } as unknown as ReturnType<typeof useFriendsModule.useFriends>)
})

describe('FriendFormDialog', () => {
  it('renders add dialog when editTarget is null', () => {
    render(<FriendFormDialog open editTarget={null} onClose={vi.fn()} />)
    expect(screen.getByText('Add Friend')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Add' })).toBeInTheDocument()
  })

  it('renders edit dialog when editTarget is provided', () => {
    const friend = { id: 1, displayName: 'Alice', discordId: null, createdAt: '' }
    render(<FriendFormDialog open editTarget={friend} onClose={vi.fn()} />)
    expect(screen.getByText('Edit Friend')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Alice')).toBeInTheDocument()
  })

  it('shows validation error when display name is empty', async () => {
    render(<FriendFormDialog open editTarget={null} onClose={vi.fn()} />)
    fireEvent.click(screen.getByRole('button', { name: 'Add' }))
    await waitFor(() => expect(screen.getByText('Display name is required')).toBeInTheDocument())
    expect(mockCreate.mutate).not.toHaveBeenCalled()
  })

  it('calls create.mutate with form values on submit', async () => {
    render(<FriendFormDialog open editTarget={null} onClose={vi.fn()} />)
    fireEvent.change(screen.getByLabelText(/display name/i), { target: { value: 'Bob' } })
    fireEvent.click(screen.getByRole('button', { name: 'Add' }))
    await waitFor(() =>
      expect(mockCreate.mutate).toHaveBeenCalledWith(
        { displayName: 'Bob', discordId: undefined },
        expect.any(Object),
      ),
    )
  })
})
