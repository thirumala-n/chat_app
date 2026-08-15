import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="app-loading" aria-label="Loading application">
        <div className="oauth-spinner" />
      </div>
    )
  }

  return user ? <Outlet /> : <Navigate to="/login" replace />
}
