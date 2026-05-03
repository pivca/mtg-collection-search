import { useEffect, useState } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Stack,
} from '@mui/material'
import { useFriends } from '../../hooks/useFriends'
import type { Friend } from '../../types'

interface Props {
  open: boolean
  editTarget: Friend | null
  onClose: () => void
}

const EMPTY = { displayName: '', discordId: '' }

const FriendFormDialog = ({ open, editTarget, onClose }: Props) => {
  const [form, setForm] = useState(EMPTY)
  const [errors, setErrors] = useState<{ displayName?: string }>({})
  const { create, update } = useFriends()

  useEffect(() => {
    if (open) {
      setForm(
        editTarget
          ? { displayName: editTarget.displayName, discordId: editTarget.discordId ?? '' }
          : EMPTY,
      )
      setErrors({})
    }
  }, [open, editTarget])

  const validate = () => {
    if (!form.displayName.trim()) {
      setErrors({ displayName: 'Display name is required' })
      return false
    }
    if (form.displayName.length > 128) {
      setErrors({ displayName: 'Max 128 characters' })
      return false
    }
    return true
  }

  const handleSubmit = () => {
    if (!validate()) return
    const body = {
      displayName: form.displayName.trim(),
      discordId: form.discordId.trim() || undefined,
    }
    if (editTarget) {
      update.mutate({ id: editTarget.id, body }, { onSuccess: onClose })
    } else {
      create.mutate(body, { onSuccess: onClose })
    }
  }

  const isPending = create.isPending || update.isPending

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{editTarget ? 'Edit Friend' : 'Add Friend'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          <TextField
            label="Display Name"
            required
            fullWidth
            value={form.displayName}
            onChange={(e) => setForm((f) => ({ ...f, displayName: e.target.value }))}
            error={!!errors.displayName}
            helperText={errors.displayName}
            slotProps={{ htmlInput: { maxLength: 128 } }}
          />
          <TextField
            label="Discord ID (optional)"
            fullWidth
            value={form.discordId}
            onChange={(e) => setForm((f) => ({ ...f, discordId: e.target.value }))}
            slotProps={{ htmlInput: { maxLength: 64 } }}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isPending}>
          Cancel
        </Button>
        <Button variant="contained" onClick={handleSubmit} disabled={isPending}>
          {editTarget ? 'Save' : 'Add'}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default FriendFormDialog
