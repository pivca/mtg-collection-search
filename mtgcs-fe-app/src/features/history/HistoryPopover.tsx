import { useState } from 'react'
import {
  IconButton,
  Popover,
  Box,
  Typography,
  List,
  ListItem,
  CircularProgress,
  Chip,
  Divider,
  Tooltip,
} from '@mui/material'
import HistoryIcon from '@mui/icons-material/History'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { fetchHistory } from './api'
import type { ActionType, CardSearchResult, HistoryEntry } from '../../types'

const ACTION_LABELS: Record<ActionType, string> = {
  CARD_SEARCH: 'Search',
  FRIEND_CREATED: 'Friend Added',
  FRIEND_UPDATED: 'Friend Updated',
  FRIEND_DELETED: 'Friend Removed',
  COLLECTION_ADDED: 'Collection Added',
  COLLECTION_DELETED: 'Collection Removed',
}

const ACTION_COLORS: Record<ActionType, string> = {
  CARD_SEARCH: '#4d96ff',
  FRIEND_CREATED: '#6bcb77',
  FRIEND_UPDATED: '#ffd93d',
  FRIEND_DELETED: '#ff6b6b',
  COLLECTION_ADDED: '#6bcb77',
  COLLECTION_DELETED: '#ff6b6b',
}

function timeAgo(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60_000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins}m ago`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs}h ago`
  return `${Math.floor(hrs / 24)}d ago`
}

const HistoryItem = ({ entry, onClick }: { entry: HistoryEntry; onClick?: () => void }) => {
  const color = ACTION_COLORS[entry.actionType]
  const label = ACTION_LABELS[entry.actionType]
  const clickable = entry.actionType === 'CARD_SEARCH' && entry.payload != null

  return (
    <ListItem
      disableGutters
      onClick={clickable ? onClick : undefined}
      sx={{
        px: 2,
        py: 0.75,
        gap: 1.5,
        alignItems: 'flex-start',
        cursor: clickable ? 'pointer' : 'default',
        borderRadius: 1,
        '&:hover': clickable ? { bgcolor: 'action.hover' } : undefined,
      }}
    >
      <Chip
        label={label}
        size="small"
        sx={{
          bgcolor: `${color}22`,
          color,
          border: `1px solid ${color}55`,
          fontWeight: 700,
          fontSize: '0.65rem',
          height: 20,
          mt: 0.25,
          flexShrink: 0,
        }}
      />
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Typography
          variant="body2"
          sx={{
            fontSize: '0.8rem',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {entry.summary}
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.disabled', fontSize: '0.7rem' }}>
          {timeAgo(entry.createdAt)}
        </Typography>
      </Box>
    </ListItem>
  )
}

const HistoryPopover = () => {
  const [anchor, setAnchor] = useState<HTMLButtonElement | null>(null)
  const open = Boolean(anchor)
  const navigate = useNavigate()

  const handleEntryClick = (entry: HistoryEntry) => {
    if (entry.actionType !== 'CARD_SEARCH' || !entry.payload) return
    try {
      const results: CardSearchResult[] = JSON.parse(entry.payload)
      const input = results.map((r) => r.cardName).join('\n')
      setAnchor(null)
      navigate('/search', { state: { input, results } })
    } catch {
      // malformed payload — ignore
    }
  }

  const { data, isFetching } = useQuery({
    queryKey: ['history'],
    queryFn: fetchHistory,
    enabled: open,
    staleTime: 0,
  })

  return (
    <>
      <Tooltip title="Recent activity">
        <IconButton
          onClick={(e) => setAnchor(e.currentTarget)}
          color="inherit"
          size="small"
          sx={{ opacity: 0.8, '&:hover': { opacity: 1 } }}
        >
          <HistoryIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Popover
        open={open}
        anchorEl={anchor}
        onClose={() => setAnchor(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        slotProps={{ paper: { sx: { width: 380, mt: 1, borderRadius: 2 } } }}
      >
        <Box sx={{ px: 2, pt: 1.5, pb: 0.5 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
            Recent activity
          </Typography>
        </Box>
        <Divider />
        {isFetching ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
            <CircularProgress size={24} />
          </Box>
        ) : !data?.length ? (
          <Box sx={{ px: 2, py: 3 }}>
            <Typography variant="body2" sx={{ color: 'text.disabled', textAlign: 'center' }}>
              No activity yet
            </Typography>
          </Box>
        ) : (
          <List disablePadding>
            {data.map((entry, i) => (
              <Box key={entry.id}>
                {i > 0 && <Divider sx={{ mx: 2 }} />}
                <HistoryItem entry={entry} onClick={() => handleEntryClick(entry)} />
              </Box>
            ))}
          </List>
        )}
      </Popover>
    </>
  )
}

export default HistoryPopover
