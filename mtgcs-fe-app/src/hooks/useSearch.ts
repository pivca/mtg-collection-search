import { useMutation } from '@tanstack/react-query'
import { searchCards } from '../features/search/api'

export const useSearch = () => {
  return useMutation({
    mutationFn: (cardNames: string[]) => searchCards(cardNames),
  })
}
