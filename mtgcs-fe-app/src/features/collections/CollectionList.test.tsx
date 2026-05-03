import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import CollectionList from './CollectionList'
import * as useCollectionsModule from '../../hooks/useCollections'
import * as friendsStoreModule from '../../store/friendsStore'

vi.mock('../../hooks/useCollections')
vi.mock('../../store/friendsStore')

const mockUseCollections = vi.mocked(useCollectionsModule.useCollections)
const mockUseFriendsStore = vi.mocked(friendsStoreModule.useFriendsStore)
const mockRemove = { mutate: vi.fn() }

type MockHook = ReturnType<typeof useCollectionsModule.useCollections>

const makeHook = (overrides: Partial<MockHook> = {}): MockHook =>
  ({
    data: undefined,
    isLoading: false,
    create: { mutate: vi.fn() },
    remove: mockRemove,
    ...overrides,
  }) as unknown as MockHook

describe('CollectionList', () => {
  beforeEach(() => vi.clearAllMocks())

  it('shows prompt when no friend selected', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: null, setSelectedFriend: vi.fn() })
    mockUseCollections.mockReturnValue(makeHook())
    render(<CollectionList />)
    expect(screen.getByText(/select a friend/i)).toBeInTheDocument()
  })

  it('shows empty state when friend selected but no collections', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    mockUseCollections.mockReturnValue(makeHook({ data: [] }))
    render(<CollectionList />)
    expect(screen.getByText(/add collection/i)).toBeInTheDocument()
  })

  it('renders collection rows', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    mockUseCollections.mockReturnValue(
      makeHook({
        data: [
          {
            id: 10,
            friendId: 1,
            friendDisplayName: 'Alice',
            sourceType: 'MOXFIELD',
            sourceUrl: 'https://moxfield.com/decks/abc',
            addedAt: '',
          },
        ],
      }),
    )
    render(<CollectionList />)
    expect(screen.getByText('Moxfield')).toBeInTheDocument()
    expect(screen.getByText('moxfield.com/decks/abc')).toBeInTheDocument()
  })

  it('calls remove.mutate on delete click', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    mockUseCollections.mockReturnValue(
      makeHook({
        data: [
          {
            id: 10,
            friendId: 1,
            friendDisplayName: 'Alice',
            sourceType: 'DECKBOX',
            sourceUrl: 'https://deckbox.org/sets/abc',
            addedAt: '',
          },
        ],
      }),
    )
    render(<CollectionList />)
    fireEvent.click(screen.getByRole('button', { name: /delete collection/i }))
    expect(mockRemove.mutate).toHaveBeenCalledWith(10)
  })
})
