import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getCollections, createCollection, deleteCollection } from '../features/collections/api'
import type { CreateCollectionRequest } from '../features/collections/types'

export const useCollections = (friendId: number | null) => {
  const queryClient = useQueryClient()
  const queryKey = ['collections', friendId]

  const query = useQuery({
    queryKey,
    queryFn: () => getCollections(friendId!),
    enabled: friendId !== null,
  })

  const create = useMutation({
    mutationFn: (body: CreateCollectionRequest) => createCollection(friendId!, body),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
  })

  const remove = useMutation({
    mutationFn: (id: number) => deleteCollection(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey }),
  })

  return { ...query, create, remove }
}
