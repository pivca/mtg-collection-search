import { Avatar, Box, Typography } from '@mui/material'
import { useAuth } from '../../hooks/useAuth'

const UserAvatar = () => {
  const { user } = useAuth()

  if (!user) return null

  return (
    <Box className="flex items-center gap-2">
      <Avatar
        src={user.avatarUrl ?? undefined}
        alt={user.username}
        sx={{ width: 32, height: 32 }}
      />
      <Typography variant="body2" color="inherit">
        {user.username}
      </Typography>
    </Box>
  )
}

export default UserAvatar
