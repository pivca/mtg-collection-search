import { useState } from 'react'
import { Box, TextField, Select, MenuItem, Button, FormControl, InputLabel } from '@mui/material'
import { useCollections } from '../../hooks/useCollections'
import { useFriendsStore } from '../../store/friendsStore'
import { SOURCE_URL_PATTERN } from './types'
import type { SourceType } from '../../types'

const AddCollectionForm = () => {
  const { selectedFriendId } = useFriendsStore()
  const { create } = useCollections(selectedFriendId)

  const [sourceType, setSourceType] = useState<SourceType>('MOXFIELD')
  const [sourceUrl, setSourceUrl] = useState('')
  const [errors, setErrors] = useState<{ sourceUrl?: string }>({})

  if (!selectedFriendId) return null

  const validate = () => {
    if (!sourceUrl.trim()) {
      setErrors({ sourceUrl: 'URL is required' })
      return false
    }
    if (!SOURCE_URL_PATTERN.test(sourceUrl.trim())) {
      setErrors({ sourceUrl: 'Must be a valid Deckbox or Moxfield URL' })
      return false
    }
    return true
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!validate()) return
    create.mutate(
      { sourceType, sourceUrl: sourceUrl.trim() },
      {
        onSuccess: () => {
          setSourceUrl('')
          setErrors({})
        },
      },
    )
  }

  return (
    <Box
      component="form"
      onSubmit={handleSubmit}
      className="flex gap-3 items-start flex-wrap p-4 border-t"
    >
      <FormControl size="small" sx={{ minWidth: 140 }}>
        <InputLabel id="source-type-label">Source</InputLabel>
        <Select
          labelId="source-type-label"
          label="Source"
          value={sourceType}
          onChange={(e) => setSourceType(e.target.value as SourceType)}
        >
          <MenuItem value="MOXFIELD">Moxfield</MenuItem>
          <MenuItem value="DECKBOX">Deckbox</MenuItem>
        </Select>
      </FormControl>

      <TextField
        label="Collection URL"
        size="small"
        value={sourceUrl}
        onChange={(e) => setSourceUrl(e.target.value)}
        error={!!errors.sourceUrl}
        helperText={errors.sourceUrl}
        sx={{ flex: 1, minWidth: 260 }}
        slotProps={{ htmlInput: { maxLength: 1024 } }}
      />

      <Button type="submit" variant="contained" disabled={create.isPending} sx={{ height: 40 }}>
        Add
      </Button>
    </Box>
  )
}

export default AddCollectionForm
