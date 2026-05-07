import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthGuard } from './features/auth'
import AppLayout from './components/ui/AppLayout'
import ErrorPage from './components/ui/ErrorPage'
import FriendsPage from './components/ui/FriendsPage'
import SearchPage from './components/ui/SearchPage'

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/error" element={<ErrorPage />} />
        <Route
          path="/*"
          element={
            <AuthGuard>
              <Routes>
                <Route element={<AppLayout />}>
                  <Route index element={<Navigate to="/friends" replace />} />
                  <Route path="/friends" element={<FriendsPage />} />
                  <Route path="/search" element={<SearchPage />} />
                </Route>
                <Route path="*" element={<Navigate to="/friends" replace />} />
              </Routes>
            </AuthGuard>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App
