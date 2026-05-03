import { useState } from 'react'
import {
  Box,
  Button,
  IconButton,
  TextField,
  Typography,
  CircularProgress,
  Chip,
  Alert,
  Collapse,
  Paper,
  Divider,
  Tooltip,
} from '@mui/material'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'
import SearchIcon from '@mui/icons-material/Search'
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import RemoveCircleOutlinedIcon from '@mui/icons-material/RemoveCircleOutlined'
import { useSearch } from '../../hooks/useSearch'
import type { CardSearchResult, CardMatch } from '../../types'

const SOURCE_COLORS: Record<string, string> = {
  MOXFIELD: '#5865F2',
  DECKBOX: '#c9920f',
}

const SOURCE_LABELS: Record<string, string> = {
  MOXFIELD: 'Moxfield',
  DECKBOX: 'Deckbox',
}

const MatchRow = ({ match }: { match: CardMatch }) => {
  const color = SOURCE_COLORS[match.sourceType] ?? '#5c5f66'
  const label = SOURCE_LABELS[match.sourceType] ?? match.sourceType

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        py: 0.75,
        px: 1.5,
        borderRadius: 1,
        bgcolor: 'background.paper',
        '&:not(:last-child)': { mb: 0.5 },
      }}
    >
      <Typography variant="body2" sx={{ fontWeight: 600, minWidth: 130 }}>
        {match.friendName}
      </Typography>
      <Chip
        label={label}
        size="small"
        sx={{
          bgcolor: `${color}22`,
          color,
          border: `1px solid ${color}55`,
          fontWeight: 700,
          fontSize: '0.7rem',
        }}
      />
      {/* Edition */}
      {match.edition && (
        <Tooltip title={match.edition} placement="top">
          <Typography
            variant="caption"
            sx={{
              color: 'text.secondary',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              maxWidth: 200,
            }}
          >
            {match.edition}
          </Typography>
        </Tooltip>
      )}
      {/* Language */}
      {match.language && (
        <Typography variant="caption" sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>
          {match.language}
        </Typography>
      )}
      {/* Foil */}
      {match.foil && (
        <Chip
          label="Foil"
          size="small"
          sx={{
            bgcolor: 'rgba(255,215,0,0.12)',
            color: '#FFD700',
            border: '1px solid rgba(255,215,0,0.4)',
            fontWeight: 700,
            fontSize: '0.7rem',
          }}
        />
      )}
      {match.cardPageUrl ? (
        <Tooltip title="View card on Deckbox" placement="top">
          <IconButton
            component="a"
            href={match.cardPageUrl}
            target="_blank"
            rel="noopener noreferrer"
            size="small"
            sx={{ color: 'text.disabled', '&:hover': { color: 'primary.main' } }}
          >
            <OpenInNewIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      ) : null}
      <Box sx={{ flex: 1 }} />
      {match.price && (
        <Typography
          variant="caption"
          sx={{ color: 'success.light', fontWeight: 600, whiteSpace: 'nowrap' }}
        >
          {match.price}
        </Typography>
      )}
      <Chip
        label={`×${match.quantity}`}
        size="small"
        variant="outlined"
        sx={{ minWidth: 40, fontWeight: 700 }}
      />
    </Box>
  )
}

const CardResultRow = ({ result }: { result: CardSearchResult }) => {
  const found = result.matches.length > 0

  return (
    <Paper
      elevation={0}
      sx={{
        borderRadius: 2,
        overflow: 'hidden',
        border: '1px solid',
        borderColor: found ? 'success.dark' : 'divider',
        bgcolor: '#2B2D31',
      }}
    >
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          px: 2,
          py: 1.25,
          bgcolor: found ? 'rgba(35,165,90,0.08)' : 'rgba(255,255,255,0.02)',
        }}
      >
        {found ? (
          <CheckCircleOutlinedIcon sx={{ color: 'success.main', fontSize: 18 }} />
        ) : (
          <RemoveCircleOutlinedIcon sx={{ color: 'text.disabled', fontSize: 18 }} />
        )}
        <Typography variant="body1" sx={{ fontWeight: 600, flex: 1 }}>
          {result.cardName}
        </Typography>
        {found && (
          <Typography variant="caption" sx={{ color: 'text.secondary' }}>
            {result.matches.length} {result.matches.length === 1 ? 'location' : 'locations'}
          </Typography>
        )}
      </Box>

      {/* Matches */}
      <Collapse in={found}>
        <Divider />
        <Box sx={{ px: 1.5, py: 1 }}>
          {result.matches.map((m, i) => (
            <MatchRow key={i} match={m} />
          ))}
        </Box>
      </Collapse>
    </Paper>
  )
}

const SearchPanel = () => {
  const [input, setInput] = useState('')
  const { mutate, data: results, isPending, error, reset } = useSearch()

  const handleSearch = () => {
    const names = input
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)

    if (names.length === 0) return
    mutate(names)
  }

  const handleInputChange = (value: string) => {
    setInput(value)
    if (results || error) reset()
  }

  const parsedCount = input
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean).length

  const foundCount = results?.filter((r) => r.matches.length > 0).length ?? 0

  return (
    <Box sx={{ display: 'flex', height: '100%', p: 3, gap: 3, overflow: 'hidden' }}>
      {/* Left column — input */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, width: 380, minWidth: 380 }}>
        <Typography variant="h6" sx={{ fontWeight: 700 }}>
          Card Search
        </Typography>
        <TextField
          multiline
          minRows={8}
          maxRows={20}
          fullWidth
          placeholder={`Paste card names, one per line:\nLightning Bolt\nCounterspell\nBlack Lotus`}
          value={input}
          onChange={(e) => handleInputChange(e.target.value)}
          slotProps={{ htmlInput: { spellCheck: false } }}
          sx={{ fontFamily: 'monospace', flex: 1 }}
        />
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={isPending ? <CircularProgress size={16} color="inherit" /> : <SearchIcon />}
            onClick={handleSearch}
            disabled={isPending || parsedCount === 0}
          >
            Search {parsedCount > 0 ? `${parsedCount} card${parsedCount > 1 ? 's' : ''}` : ''}
          </Button>
          {results && (
            <Typography variant="body2" sx={{ color: 'text.secondary' }}>
              Found{' '}
              <Box component="span" sx={{ color: 'success.main', fontWeight: 700 }}>
                {foundCount}
              </Box>{' '}
              / {results.length} cards
            </Typography>
          )}
        </Box>

        {error && (
          <Alert severity="error" onClose={reset}>
            Search failed. Please try again.
          </Alert>
        )}
      </Box>

      {/* Right column — results */}
      <Box sx={{ flex: 1, overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: 1.5 }}>
        {results ? (
          results.map((r) => <CardResultRow key={r.cardName} result={r} />)
        ) : (
          <Box
            sx={{
              flex: 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Typography variant="body2" sx={{ color: 'text.disabled' }}>
              Results will appear here
            </Typography>
          </Box>
        )}
      </Box>
    </Box>
  )
}

export default SearchPanel
