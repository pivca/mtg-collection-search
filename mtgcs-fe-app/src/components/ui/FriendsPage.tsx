import { useState } from 'react'
import { Box, Typography, Button } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import { FriendList, FriendFormDialog } from '../../features/friends'
import { CollectionList } from '../../features/collections'
import type { Friend } from '../../types'

const FriendsPage = () => {
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editTarget, setEditTarget] = useState<Friend | null>(null)

  const openAdd = () => {
    setEditTarget(null)
    setDialogOpen(true)
  }

  const openEdit = (friend: Friend) => {
    setEditTarget(friend)
    setDialogOpen(true)
  }

  return (
    <Box sx={{ display: 'flex', height: '100%', overflow: 'hidden' }}>
      {/* Sidebar */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          width: 240,
          minWidth: 240,
          bgcolor: '#2B2D31',
          borderRight: '1px solid',
          borderColor: 'divider',
          overflowY: 'auto',
        }}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 2,
            py: 1.5,
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Typography variant="subtitle1" sx={{ color: 'text.secondary' }}>
            Friends
          </Typography>
          <Button
            size="small"
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openAdd}
            sx={{ minWidth: 0, px: 1.5, py: 0.5, fontSize: '0.75rem' }}
          >
            Add
          </Button>
        </Box>
        <FriendList onEdit={openEdit} />
      </Box>

      {/* Main — Collections */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          overflow: 'hidden',
          bgcolor: 'background.default',
        }}
      >
        <Box sx={{ flex: 1, overflowY: 'auto' }}>
          <CollectionList />
        </Box>
      </Box>

      <FriendFormDialog
        open={dialogOpen}
        editTarget={editTarget}
        onClose={() => setDialogOpen(false)}
      />
    </Box>
  )
}

export default FriendsPage
