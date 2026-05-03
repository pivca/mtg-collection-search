import { NavLink, Outlet } from 'react-router-dom'
import { AppBar, Toolbar, Typography, Box, Button } from '@mui/material'
import PeopleIcon from '@mui/icons-material/People'
import SearchIcon from '@mui/icons-material/Search'
import UserAvatar from './UserAvatar'

const NAV_LINK_SX = {
  textTransform: 'none',
  fontSize: '0.875rem',
  fontWeight: 500,
  color: 'inherit',
  opacity: 0.75,
  '&.active': { opacity: 1 },
  '&:hover': { opacity: 1 },
}

const AppLayout = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>
      <AppBar position="static">
        <Toolbar sx={{ gap: 1 }}>
          <Typography variant="h6" sx={{ mr: 2 }}>
            MTG Collection Search
          </Typography>

          <Button
            component={NavLink}
            to="/friends"
            startIcon={<PeopleIcon sx={{ fontSize: 18 }} />}
            sx={NAV_LINK_SX}
          >
            Friends
          </Button>

          <Button
            component={NavLink}
            to="/search"
            startIcon={<SearchIcon sx={{ fontSize: 18 }} />}
            sx={NAV_LINK_SX}
          >
            Search
          </Button>

          <Box sx={{ flex: 1 }} />
          <UserAvatar />
        </Toolbar>
      </AppBar>

      <Box sx={{ flex: 1, overflow: 'hidden' }}>
        <Outlet />
      </Box>
    </Box>
  )
}

export default AppLayout
