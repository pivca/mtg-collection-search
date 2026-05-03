import { useState } from 'react'
import { Box, Typography, Paper, IconButton, Skeleton, Tooltip } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import AddIcon from '@mui/icons-material/Add'
import { useCollections } from '../../hooks/useCollections'
import { useFriendsStore } from '../../store/friendsStore'
import AddCollectionDialog from './AddCollectionDialog'
import type { Collection } from '../../types'

const SOURCE_COLORS: Record<string, string> = {
  MOXFIELD: '#5865F2',
  DECKBOX: '#c9920f',
}

const SOURCE_LABELS: Record<string, string> = {
  MOXFIELD: 'Moxfield',
  DECKBOX: 'Deckbox',
}

const CollectionCard = ({
  collection,
  onDelete,
}: {
  collection: Collection
  onDelete: () => void
}) => {
  const color = SOURCE_COLORS[collection.sourceType] ?? '#5c5f66'
  const label = SOURCE_LABELS[collection.sourceType] ?? collection.sourceType

  // Show just the meaningful path part of the URL
  let displayUrl = collection.sourceUrl
  try {
    const u = new URL(collection.sourceUrl)
    displayUrl = u.hostname.replace(/^www\./, '') + u.pathname
  } catch {
    /* keep original */
  }

  return (
    <Paper
      elevation={0}
      sx={{
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        borderRadius: 2,
        overflow: 'hidden',
        bgcolor: '#2B2D31',
        border: '1px solid',
        borderColor: 'divider',
        transition: 'transform 0.15s, box-shadow 0.15s',
        '&:hover': {
          transform: 'translateY(-3px)',
          boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
          '& .delete-btn': { opacity: 1 },
        },
      }}
    >
      {/* Colour header stripe */}
      <Box sx={{ height: 6, bgcolor: color }} />

      <Box sx={{ p: 2, flex: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Typography
          variant="caption"
          sx={{
            fontWeight: 700,
            color,
            textTransform: 'uppercase',
            letterSpacing: '0.08em',
          }}
        >
          {label}
        </Typography>

        <Tooltip title={collection.sourceUrl} placement="bottom-start">
          <Typography
            variant="body2"
            sx={{
              color: 'text.secondary',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              fontSize: '0.78rem',
            }}
          >
            {displayUrl}
          </Typography>
        </Tooltip>
      </Box>

      <IconButton
        className="delete-btn"
        size="small"
        aria-label="delete collection"
        onClick={onDelete}
        sx={{
          position: 'absolute',
          top: 10,
          right: 8,
          opacity: 0,
          transition: 'opacity 0.15s',
          color: 'text.secondary',
          '&:hover': { color: 'error.main' },
        }}
      >
        <DeleteIcon fontSize="small" />
      </IconButton>
    </Paper>
  )
}

const AddCard = ({ onClick }: { onClick: () => void }) => (
  <Paper
    elevation={0}
    onClick={onClick}
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      gap: 1,
      borderRadius: 2,
      border: '2px dashed',
      borderColor: 'divider',
      bgcolor: 'transparent',
      cursor: 'pointer',
      minHeight: 110,
      transition: 'border-color 0.15s, background 0.15s',
      '&:hover': {
        borderColor: 'primary.main',
        bgcolor: 'rgba(88,101,242,0.06)',
        '& .add-icon': { color: 'primary.main' },
        '& .add-label': { color: 'primary.main' },
      },
    }}
  >
    <AddIcon
      className="add-icon"
      sx={{ color: 'text.disabled', fontSize: 32, transition: 'color 0.15s' }}
    />
    <Typography
      className="add-label"
      variant="caption"
      sx={{ color: 'text.disabled', fontWeight: 600, transition: 'color 0.15s' }}
    >
      Add Collection
    </Typography>
  </Paper>
)

const CollectionList = () => {
  const { selectedFriendId } = useFriendsStore()
  const { data: collections, isLoading, remove } = useCollections(selectedFriendId)
  const [dialogOpen, setDialogOpen] = useState(false)

  if (!selectedFriendId) {
    return (
      <Box className="flex items-center justify-center h-full">
        <Typography variant="body1" color="text.secondary">
          Select a friend to view their collections.
        </Typography>
      </Box>
    )
  }

  if (isLoading) {
    return (
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
          gap: 2,
          p: 3,
        }}
      >
        {[1, 2, 3].map((i) => (
          <Skeleton key={i} variant="rectangular" height={110} sx={{ borderRadius: 2 }} />
        ))}
      </Box>
    )
  }

  return (
    <>
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
          gap: 2,
          p: 3,
          alignContent: 'start',
        }}
      >
        {collections?.map((col) => (
          <CollectionCard key={col.id} collection={col} onDelete={() => remove.mutate(col.id)} />
        ))}
        <AddCard onClick={() => setDialogOpen(true)} />
      </Box>

      <AddCollectionDialog
        open={dialogOpen}
        friendId={selectedFriendId}
        onClose={() => setDialogOpen(false)}
      />
    </>
  )
}

export default CollectionList
