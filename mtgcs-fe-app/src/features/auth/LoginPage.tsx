import { Box, Button, Typography } from '@mui/material'

export const LoginPage = () => {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 3,
        bgcolor: '#1E1F22',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
          p: 5,
          bgcolor: '#2B2D31',
          borderRadius: 2,
          border: '1px solid #3F4147',
          minWidth: 360,
        }}
      >
        <Typography variant="h5" sx={{ fontWeight: 700, color: '#DBDEE1' }}>
          MTG Collection Search
        </Typography>
        <Typography variant="body2" sx={{ color: '#80848E', textAlign: 'center' }}>
          Sign in to manage your friends&apos; card collections.
        </Typography>
        <Button
          variant="contained"
          size="large"
          fullWidth
          href="/oauth2/authorization/discord"
          sx={{
            mt: 1,
            backgroundColor: '#5865F2',
            '&:hover': { backgroundColor: '#4752C4' },
            fontWeight: 600,
            py: 1.25,
          }}
        >
          Login with Discord
        </Button>
      </Box>
    </Box>
  )
}
