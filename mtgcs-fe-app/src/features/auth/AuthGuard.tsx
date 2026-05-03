import { CircularProgress, Box } from '@mui/material'
import { useAuth } from '../../hooks/useAuth'
import { LoginPage } from './LoginPage'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
}

const AuthGuard = ({ children }: Props) => {
  const { isAuthenticated, isLoading } = useAuth()

  if (isLoading) {
    return (
      <Box className="min-h-screen flex items-center justify-center">
        <CircularProgress />
      </Box>
    )
  }

  if (!isAuthenticated) {
    return <LoginPage />
  }

  return <>{children}</>
}

export default AuthGuard
