import { Chip } from '@mui/material'

const FoilChip = () => (
  <Chip
    label="Foil"
    size="small"
    sx={{
      background: 'linear-gradient(90deg, #ff6b6b, #ffd93d, #6bcb77, #4d96ff, #c77dff)',
      backgroundSize: '200% 200%',
      color: '#fff',
      fontWeight: 700,
      fontSize: '0.7rem',
      border: 'none',
      textShadow: '0 1px 2px rgba(0,0,0,0.4)',
      animation: 'foilShimmer 3s linear infinite',
      '@keyframes foilShimmer': {
        '0%': { backgroundPosition: '0% 50%' },
        '50%': { backgroundPosition: '100% 50%' },
        '100%': { backgroundPosition: '0% 50%' },
      },
    }}
  />
)

export default FoilChip
