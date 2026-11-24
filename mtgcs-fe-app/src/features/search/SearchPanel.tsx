import { useState, useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import {
  Box,
  Button,
  TextField,
  Typography,
  CircularProgress,
  Alert,
  Snackbar,
} from '@mui/material'
import SearchIcon from '@mui/icons-material/Search'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import { useSearch } from '../../hooks/useSearch'
import CardResultRow from './CardResultRow'
import type { CardSearchResult, SearchResponse } from '../../types'

const SearchPanel = () => {
  const location = useLocation()
  const locationState = location.state as {
    input?: string
    response?: SearchResponse
    results?: CardSearchResult[]
  } | null

  const [input, setInput] = useState(locationState?.input ?? '')
  const [preloaded, setPreloaded] = useState<SearchResponse | null>(
    locationState?.response ??
      (locationState?.results
        ? { results: locationState.results, buylist: [], formattedBuylist: '' }
        : null),
  )
  const [copyMessage, setCopyMessage] = useState<string | null>(null)
  const { mutate, data: mutationResults, isPending, error, reset } = useSearch()

  useEffect(() => {
    if (
      locationState?.input !== undefined ||
      locationState?.response !== undefined ||
      locationState?.results !== undefined
    ) {
      let active = true

      queueMicrotask(() => {
        if (!active) return
        setInput(locationState.input ?? '')
        setPreloaded(
          locationState.response ??
            (locationState.results
              ? { results: locationState.results, buylist: [], formattedBuylist: '' }
              : null),
        )
        reset()
      })

      return () => {
        active = false
      }
    }
  }, [location.state]) // eslint-disable-line react-hooks/exhaustive-deps

  const response = preloaded ?? mutationResults
  const results = response?.results
  const formattedBuylist = response?.formattedBuylist

  const handleSearch = () => {
    const names = input
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)
    if (names.length === 0) return
    setPreloaded(null)
    mutate(names)
  }

  const handleInputChange = (value: string) => {
    setInput(value)
    if (preloaded) setPreloaded(null)
    if (mutationResults || error) reset()
  }

  const handleCopyFoundByFriend = async () => {
    if (!formattedBuylist) return

    try {
      await navigator.clipboard.writeText(formattedBuylist)
      setCopyMessage('Copied found cards grouped by friend.')
    } catch {
      setCopyMessage('Could not copy results. Please try again.')
    }
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
          {results && (
            <Button
              variant="outlined"
              size="small"
              startIcon={<ContentCopyIcon />}
              onClick={handleCopyFoundByFriend}
              disabled={!formattedBuylist}
            >
              Copy found by friend
            </Button>
          )}
        </Box>
        {error && (
          <Alert severity="error" onClose={reset}>
            Search failed. Please try again.
          </Alert>
        )}
      </Box>

      {/* Right column — results */}
      <Box sx={{ flex: 1, minHeight: 0, overflow: 'hidden', position: 'relative' }}>
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
            pr: 0.5,
          }}
        >
          {results ? (
            results.map((r) => <CardResultRow key={r.cardName} result={r} />)
          ) : (
            <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Typography variant="body2" sx={{ color: 'text.disabled' }}>
                Results will appear here
              </Typography>
            </Box>
          )}
        </Box>
      </Box>
      <Snackbar
        open={Boolean(copyMessage)}
        autoHideDuration={3000}
        onClose={() => setCopyMessage(null)}
        message={copyMessage}
      />
    </Box>
  )
}

export default SearchPanel
