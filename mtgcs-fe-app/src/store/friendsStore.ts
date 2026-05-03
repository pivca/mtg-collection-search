import { create } from 'zustand'

interface FriendsStore {
  selectedFriendId: number | null
  setSelectedFriend: (id: number | null) => void
}

export const useFriendsStore = create<FriendsStore>((set) => ({
  selectedFriendId: null,
  setSelectedFriend: (id) => set({ selectedFriendId: id }),
}))
