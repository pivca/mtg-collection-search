import { Box, Chip, IconButton, Tooltip, Typography } from '@mui/material'
import OpenInNewIcon from '@mui/icons-material/OpenInNew'
import { SOURCE_COLORS, SOURCE_LABELS } from '../../lib/sourceConfig'
import CardImagePreview from './CardImagePreview'
import FoilChip from './FoilChip'
import type { CardMatch } from '../../types'

interface Props {
  match: CardMatch
  cardName: string
}

const MatchRow = ({ match, cardName }: Props) => {
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

      {match.edition && (
        <CardImagePreview cardName={cardName} setCode={match.setCode}>
          <Tooltip title={match.edition} placement="top">
            <Typography
              variant="caption"
              sx={{
                color: 'text.secondary',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                maxWidth: 200,
                cursor: 'default',
              }}
            >
              {match.edition}
            </Typography>
          </Tooltip>
        </CardImagePreview>
      )}

      <Box sx={{ flex: 1 }} />

      {match.language && (
        <Typography variant="caption" sx={{ color: 'text.secondary', whiteSpace: 'nowrap' }}>
          {match.language}
        </Typography>
      )}

      {match.foil && <FoilChip />}

      {match.cardPageUrl && (
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
      )}

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

export default MatchRow
