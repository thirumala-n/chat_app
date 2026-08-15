import { LogOut } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

function getInitials(user) {
  if (!user) return '?'
  if (user.firstName && user.lastName) return (user.firstName[0] + user.lastName[0]).toUpperCase()
  if (user.username) return user.username[0].toUpperCase()
  if (user.email) return user.email[0].toUpperCase()
  return '?'
}

export default function UserMenu() {
  const { user, logout } = useAuth()

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 6, padding: '0 4px' }}>
      <div className="user-menu-btn" style={{ flex: 1 }}>
        <div className="user-avatar" aria-hidden="true">
          {user?.profileImageUrl ? (
            <img src={user.profileImageUrl} alt={user.username || 'avatar'} />
          ) : (
            getInitials(user)
          )}
        </div>
        <div className="user-info">
          <div className="user-name">
            {user?.firstName && user?.lastName
              ? `${user.firstName} ${user.lastName}`
              : user?.username || user?.email || 'User'}
          </div>
          <div className="user-email">{user?.email}</div>
        </div>
      </div>

      <button
        onClick={logout}
        className="icon-btn"
        title="Sign out"
        aria-label="Sign out"
      >
        <LogOut size={15} />
      </button>
    </div>
  )
}
