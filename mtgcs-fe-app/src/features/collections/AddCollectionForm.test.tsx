import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import AddCollectionForm from './AddCollectionForm'
import * as useCollectionsModule from '../../hooks/useCollections'
import * as friendsStoreModule from '../../store/friendsStore'

vi.mock('../../hooks/useCollections')
vi.mock('../../store/friendsStore')

const mockUseCollections = vi.mocked(useCollectionsModule.useCollections)
const mockUseFriendsStore = vi.mocked(friendsStoreModule.useFriendsStore)
const mockCreate = { mutate: vi.fn(), isPending: false }

type MockHook = ReturnType<typeof useCollectionsModule.useCollections>

const makeHook = (overrides: Partial<MockHook> = {}): MockHook =>
  ({
    data: [],
    isLoading: false,
    create: mockCreate,
    remove: { mutate: vi.fn() },
    ...overrides,
  }) as unknown as MockHook

beforeEach(() => {
  vi.clearAllMocks()
  mockUseCollections.mockReturnValue(makeHook())
})

describe('AddCollectionForm', () => {
  it('renders nothing when no friend selected', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: null, setSelectedFriend: vi.fn() })
    const { container } = render(<AddCollectionForm />)
    expect(container).toBeEmptyDOMElement()
  })

  it('renders form when friend is selected', () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    render(<AddCollectionForm />)
    expect(screen.getByLabelText(/collection url/i)).toBeInTheDocument()
  })

  it('shows validation error for invalid URL', async () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    render(<AddCollectionForm />)
    fireEvent.change(screen.getByLabelText(/collection url/i), {
      target: { value: 'https://example.com/bad' },
    })
    fireEvent.click(screen.getByRole('button', { name: /add/i }))
    await waitFor(() =>
      expect(screen.getByText(/valid deckbox or moxfield url/i)).toBeInTheDocument(),
    )
    expect(mockCreate.mutate).not.toHaveBeenCalled()
  })

  it('calls create.mutate with valid Moxfield URL', async () => {
    mockUseFriendsStore.mockReturnValue({ selectedFriendId: 1, setSelectedFriend: vi.fn() })
    render(<AddCollectionForm />)
    fireEvent.change(screen.getByLabelText(/collection url/i), {
      target: { value: 'https://moxfield.com/decks/abc123' },
    })
    fireEvent.click(screen.getByRole('button', { name: /add/i }))
    await waitFor(() =>
      expect(mockCreate.mutate).toHaveBeenCalledWith(
        { sourceType: 'MOXFIELD', sourceUrl: 'https://moxfield.com/decks/abc123' },
        expect.any(Object),
      ),
    )
  })
})
