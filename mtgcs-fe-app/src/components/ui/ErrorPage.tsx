import ErrorIcon from '@mui/icons-material/Error'
import LoginIcon from '@mui/icons-material/Login'
import { Box, Button, Typography } from '@mui/material'
import { useSearchParams } from 'react-router-dom'

const FALLBACK_MESSAGE = 'Something went wrong. Please try again.'

const ErrorPage = () => {
  const [searchParams] = useSearchParams()
  const message =
    searchParams.get('message') ||
    searchParams.get('error_description') ||
    searchParams.get('error') ||
    FALLBACK_MESSAGE

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 3,
        bgcolor: 'background.default',
        px: 2,
      }}
    >
      <Box
        sx={{
          width: '100%',
          maxWidth: 440,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
          p: { xs: 3, sm: 5 },
          bgcolor: 'background.paper',
          borderRadius: 2,
          border: '1px solid',
          borderColor: 'divider',
          textAlign: 'center',
        }}
      >
        <ErrorIcon color="error" sx={{ fontSize: 48 }} />
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Something went wrong
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', overflowWrap: 'anywhere' }}>
          {message}
        </Typography>
        <Button
          variant="contained"
          size="large"
          fullWidth
          href="/oauth2/authorization/discord"
          startIcon={<LoginIcon />}
          sx={{ mt: 1, fontWeight: 600, py: 1.25 }}
        >
          Try Discord login again
        </Button>
      </Box>
    </Box>
  )
}

export default ErrorPage
