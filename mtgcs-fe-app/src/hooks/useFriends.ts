import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getFriends, createFriend, updateFriend, deleteFriend } from '../features/friends/api'
import type { CreateFriendRequest, UpdateFriendRequest } from '../features/friends/types'

const FRIENDS_KEY = ['friends']

export const useFriends = () => {
  const queryClient = useQueryClient()

  const query = useQuery({ queryKey: FRIENDS_KEY, queryFn: getFriends })

  const create = useMutation({
    mutationFn: (body: CreateFriendRequest) => createFriend(body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: FRIENDS_KEY }),
  })

  const update = useMutation({
    mutationFn: ({ id, body }: { id: number; body: UpdateFriendRequest }) => updateFriend(id, body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: FRIENDS_KEY }),
  })

  const remove = useMutation({
    mutationFn: (id: number) => deleteFriend(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: FRIENDS_KEY }),
  })

  return { ...query, create, update, remove }
}
