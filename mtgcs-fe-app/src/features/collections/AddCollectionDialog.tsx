import { useEffect, useState } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Stack,
  Chip,
} from '@mui/material'
import { useCollections } from '../../hooks/useCollections'
import { SOURCE_URL_PATTERN, parseSourceType } from './types'

interface Props {
  open: boolean
  friendId: number
  onClose: () => void
}

const AddCollectionDialog = ({ open, friendId, onClose }: Props) => {
  const [url, setUrl] = useState('')
  const [error, setError] = useState<string | undefined>()
  const { create } = useCollections(friendId)

  useEffect(() => {
    if (open) {
      setUrl('')
      setError(undefined)
    }
  }, [open])

  const detectedType = parseSourceType(url)

  const validate = () => {
    if (!url.trim()) {
      setError('URL is required')
      return false
    }
    if (!SOURCE_URL_PATTERN.test(url.trim())) {
      setError('Must be a valid Moxfield or Deckbox URL')
      return false
    }
    return true
  }

  const handleSubmit = () => {
    if (!validate()) return
    const sourceType = parseSourceType(url.trim())!
    create.mutate({ sourceType, sourceUrl: url.trim() }, { onSuccess: onClose })
  }

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Add Collection</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <TextField
            label="Collection URL"
            fullWidth
            required
            value={url}
            onChange={(e) => {
              setUrl(e.target.value)
              setError(undefined)
            }}
            error={!!error}
            helperText={error ?? 'Paste a Moxfield or Deckbox collection URL'}
            placeholder="https://www.moxfield.com/users/..."
            slotProps={{ htmlInput: { maxLength: 1024 } }}
            autoFocus
          />
          {detectedType && (
            <Chip
              label={detectedType === 'MOXFIELD' ? 'Moxfield' : 'Deckbox'}
              size="small"
              color={detectedType === 'MOXFIELD' ? 'primary' : 'warning'}
              sx={{ alignSelf: 'flex-start' }}
            />
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={create.isPending}>
          Cancel
        </Button>
        <Button
          variant="contained"
          onClick={handleSubmit}
          disabled={create.isPending || !detectedType}
        >
          Add
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AddCollectionDialog
