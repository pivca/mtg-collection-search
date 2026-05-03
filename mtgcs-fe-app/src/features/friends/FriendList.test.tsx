import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import FriendList from './FriendList'
import * as useFriendsModule from '../../hooks/useFriends'
import * as friendsStoreModule from '../../store/friendsStore'

vi.mock('../../hooks/useFriends')
vi.mock('../../store/friendsStore')

const mockUseFriends = vi.mocked(useFriendsModule.useFriends)
const mockUseFriendsStore = vi.mocked(friendsStoreModule.useFriendsStore)

const mockRemove = { mutate: vi.fn() }

type MockHook = ReturnType<typeof useFriendsModule.useFriends>

const makeHook = (overrides: Partial<MockHook> = {}): MockHook =>
  ({
    data: undefined,
    isLoading: false,
    create: { mutate: vi.fn() },
    update: { mutate: vi.fn() },
    remove: mockRemove,
    ...overrides,
  }) as unknown as MockHook

describe('FriendList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: null, setSelectedFriend: vi.fn() })
  })

  it('shows skeletons while loading', () => {
    mockUseFriends.mockReturnValue(makeHook({ isLoading: true }))
    render(<FriendList onEdit={vi.fn()} />)
    expect(document.querySelectorAll('.MuiSkeleton-root').length).toBeGreaterThan(0)
  })

  it('shows empty state when no friends', () => {
    mockUseFriends.mockReturnValue(makeHook({ data: [] }))
    render(<FriendList onEdit={vi.fn()} />)
    expect(screen.getByText(/no friends yet/i)).toBeInTheDocument()
  })

  it('renders friend list', () => {
    mockUseFriends.mockReturnValue(
      makeHook({ data: [{ id: 1, displayName: 'Alice', discordId: null, createdAt: '' }] }),
    )
    render(<FriendList onEdit={vi.fn()} />)
    expect(screen.getByText('Alice')).toBeInTheDocument()
  })

  it('calls onEdit when edit button clicked', () => {
    const friend = { id: 1, displayName: 'Alice', discordId: null, createdAt: '' }
    mockUseFriends.mockReturnValue(makeHook({ data: [friend] }))
    const onEdit = vi.fn()
    render(<FriendList onEdit={onEdit} />)
    fireEvent.click(screen.getByRole('button', { name: /edit alice/i }))
    expect(onEdit).toHaveBeenCalledWith(friend)
  })

  it('calls remove.mutate when delete button clicked', () => {
    const friend = { id: 1, displayName: 'Alice', discordId: null, createdAt: '' }
    mockUseFriends.mockReturnValue(makeHook({ data: [friend] }))
    render(<FriendList onEdit={vi.fn()} />)
    fireEvent.click(screen.getByRole('button', { name: /delete alice/i }))
    expect(mockRemove.mutate).toHaveBeenCalledWith(1)
  })
})
