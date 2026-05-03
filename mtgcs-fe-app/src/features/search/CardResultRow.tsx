import { useState } from 'react'
import { Box, Collapse, Divider, Paper, Typography } from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import CheckCircleOutlinedIcon from '@mui/icons-material/CheckCircleOutlined'
import RemoveCircleOutlinedIcon from '@mui/icons-material/RemoveCircleOutlined'
import MatchRow from './MatchRow'
import type { CardSearchResult } from '../../types'

interface Props {
  result: CardSearchResult
}

const CardResultRow = ({ result }: Props) => {
  const found = result.matches.length > 0
  const [open, setOpen] = useState(true)

  return (
    <Paper
      elevation={0}
      sx={{
        borderRadius: 2,
        border: '1px solid',
        borderColor: found ? 'success.dark' : 'divider',
        bgcolor: '#2B2D31',
      }}
    >
      <Box
        onClick={() => found && setOpen((o) => !o)}
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          px: 2,
          py: 1.25,
          bgcolor: found ? 'rgba(35,165,90,0.08)' : 'rgba(255,255,255,0.02)',
          cursor: found ? 'pointer' : 'default',
          userSelect: 'none',
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
        {found && (
          <ExpandMoreIcon
            sx={{
              color: 'text.secondary',
              fontSize: 18,
              transition: 'transform 0.2s',
              transform: open ? 'rotate(180deg)' : 'rotate(0deg)',
            }}
          />
        )}
      </Box>

      <Collapse in={found && open}>
        <Divider />
        <Box sx={{ px: 1.5, py: 1 }}>
          {result.matches.map((m, i) => (
            <MatchRow key={i} match={m} cardName={result.cardName} />
          ))}
        </Box>
      </Collapse>
    </Paper>
  )
}

export default CardResultRow
