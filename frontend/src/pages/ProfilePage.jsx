import { useState, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowLeft, Camera, Check, AlertCircle, LogOut, Key, User as UserIcon, Shield } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { userApi } from '../api/services'
import ThemeToggle from '../components/ThemeToggle'

function getInitials(user) {
  if (!user) return '?'
  if (user.firstName && user.lastName) return (user.firstName[0] + user.lastName[0]).toUpperCase()
  if (user.username) return user.username[0].toUpperCase()
  if (user.email) return user.email[0].toUpperCase()
  return '?'
}

export default function ProfilePage() {
  const { user, updateUser, logout } = useAuth()
  const navigate = useNavigate()
  const fileInputRef = useRef(null)

  // Profile form state
  const [firstName, setFirstName] = useState(user?.firstName || '')
  const [lastName, setLastName] = useState(user?.lastName || '')
  const [bio, setBio] = useState(user?.bio || '')
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileSuccess, setProfileSuccess] = useState('')
  const [profileError, setProfileError] = useState('')

  // Avatar upload state
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const [avatarError, setAvatarError] = useState('')

  // Change password state
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [savingPassword, setSavingPassword] = useState(false)
  const [passwordSuccess, setPasswordSuccess] = useState('')
  const [passwordError, setPasswordError] = useState('')

  const handleUpdateProfile = async (e) => {
    e.preventDefault()
    setProfileSuccess('')
    setProfileError('')
    setSavingProfile(true)

    try {
      const { data } = await userApi.updateProfile({ firstName, lastName, bio })
      updateUser(data.data)
      setProfileSuccess('Profile updated successfully.')
      setTimeout(() => setProfileSuccess(''), 4000)
    } catch (err) {
      setProfileError(err.response?.data?.message || 'Failed to update profile.')
    } finally {
      setSavingProfile(false)
    }
  }

  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setAvatarError('')
    setUploadingAvatar(true)

    try {
      const { data } = await userApi.uploadAvatar(file)
      updateUser(data.data)
      setProfileSuccess('Profile picture updated.')
      setTimeout(() => setProfileSuccess(''), 4000)
    } catch (err) {
      setAvatarError(err.response?.data?.message || 'Failed to upload image.')
    } finally {
      setUploadingAvatar(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const handleChangePassword = async (e) => {
    e.preventDefault()
    setPasswordSuccess('')
    setPasswordError('')

    if (newPassword.length < 8) {
      setPasswordError('New password must be at least 8 characters long.')
      return
    }

    if (newPassword !== confirmPassword) {
      setPasswordError('New passwords do not match.')
      return
    }

    setSavingPassword(true)

    try {
      await userApi.changePassword({ currentPassword, newPassword })
      setPasswordSuccess('Password changed successfully.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setTimeout(() => setPasswordSuccess(''), 4000)
    } catch (err) {
      setPasswordError(err.response?.data?.message || 'Failed to change password.')
    } finally {
      setSavingPassword(false)
    }
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div className="profile-page-root">
      {/* Top Bar */}
      <header className="profile-header">
        <div className="profile-header-inner">
          <Link to="/chat" className="profile-back-link" aria-label="Back to chat">
            <ArrowLeft size={18} />
            <span>Back to chat</span>
          </Link>
          <div className="profile-header-actions">
            <ThemeToggle />
          </div>
        </div>
      </header>

      {/* Main Profile Container */}
      <main className="profile-main">
        <div className="profile-card">
          {/* Avatar Section */}
          <div className="profile-avatar-header">
            <div className="profile-avatar-wrapper">
              <div className="profile-avatar-large" aria-hidden="true">
                {user?.profileImageUrl ? (
                  <img src={user.profileImageUrl} alt={user.username || 'avatar'} />
                ) : (
                  getInitials(user)
                )}
              </div>
              <button
                type="button"
                className="profile-avatar-upload-btn"
                onClick={() => fileInputRef.current?.click()}
                disabled={uploadingAvatar}
                title="Change profile picture"
                aria-label="Change profile picture"
              >
                <Camera size={14} />
              </button>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleAvatarChange}
                style={{ display: 'none' }}
              />
            </div>

            <div className="profile-user-summary">
              <h1 className="profile-user-name">
                {user?.firstName && user?.lastName
                  ? `${user.firstName} ${user.lastName}`
                  : user?.username || 'User Profile'}
              </h1>
              <p className="profile-user-email">{user?.email}</p>
              <div className="profile-badges">
                <span className="profile-badge">@{user?.username}</span>
                {user?.emailVerified ? (
                  <span className="profile-badge badge-verified">
                    <Check size={12} /> Verified
                  </span>
                ) : (
                  <span className="profile-badge badge-unverified">
                    <Shield size={12} /> Unverified
                  </span>
                )}
              </div>
            </div>
          </div>

          {avatarError && (
            <div className="profile-alert alert-error">
              <AlertCircle size={15} />
              <span>{avatarError}</span>
            </div>
          )}

          {/* Edit Profile Form */}
          <section className="profile-section">
            <div className="profile-section-title">
              <UserIcon size={16} />
              <h2>Personal Information</h2>
            </div>

            {profileSuccess && (
              <div className="profile-alert alert-success">
                <Check size={15} />
                <span>{profileSuccess}</span>
              </div>
            )}
            {profileError && (
              <div className="profile-alert alert-error">
                <AlertCircle size={15} />
                <span>{profileError}</span>
              </div>
            )}

            <form onSubmit={handleUpdateProfile} className="profile-form">
              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="firstName">First Name</label>
                  <input
                    id="firstName"
                    type="text"
                    className="form-input"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    maxLength={100}
                    placeholder="First name"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="lastName">Last Name</label>
                  <input
                    id="lastName"
                    type="text"
                    className="form-input"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    maxLength={100}
                    placeholder="Last name"
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="email">Email</label>
                <input
                  id="email"
                  type="email"
                  className="form-input form-input-disabled"
                  value={user?.email || ''}
                  disabled
                  readOnly
                  title="Email cannot be modified"
                />
              </div>

              <div className="form-group">
                <label className="form-label" htmlFor="bio">Bio</label>
                <textarea
                  id="bio"
                  className="form-input profile-textarea"
                  value={bio}
                  onChange={(e) => setBio(e.target.value)}
                  maxLength={500}
                  rows={3}
                  placeholder="Tell us a little about yourself..."
                />
              </div>

              <button
                type="submit"
                className="btn-primary profile-submit-btn"
                disabled={savingProfile}
              >
                {savingProfile ? 'Saving changes...' : 'Save Profile'}
              </button>
            </form>
          </section>

          {/* Change Password Section */}
          <section className="profile-section">
            <div className="profile-section-title">
              <Key size={16} />
              <h2>Change Password</h2>
            </div>

            {passwordSuccess && (
              <div className="profile-alert alert-success">
                <Check size={15} />
                <span>{passwordSuccess}</span>
              </div>
            )}
            {passwordError && (
              <div className="profile-alert alert-error">
                <AlertCircle size={15} />
                <span>{passwordError}</span>
              </div>
            )}

            <form onSubmit={handleChangePassword} className="profile-form">
              <div className="form-group">
                <label className="form-label" htmlFor="currentPassword">Current Password</label>
                <input
                  id="currentPassword"
                  type="password"
                  className="form-input"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  required
                  placeholder="••••••••"
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label" htmlFor="newPassword">New Password</label>
                  <input
                    id="newPassword"
                    type="password"
                    className="form-input"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    required
                    minLength={8}
                    placeholder="Min. 8 characters"
                  />
                </div>
                <div className="form-group">
                  <label className="form-label" htmlFor="confirmPassword">Confirm New Password</label>
                  <input
                    id="confirmPassword"
                    type="password"
                    className="form-input"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                    minLength={8}
                    placeholder="Confirm new password"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="btn-primary profile-submit-btn"
                disabled={savingPassword}
              >
                {savingPassword ? 'Updating password...' : 'Update Password'}
              </button>
            </form>
          </section>

          {/* Account Actions */}
          <section className="profile-section profile-section-danger">
            <div className="profile-danger-box">
              <div>
                <h3 className="profile-danger-title">Session</h3>
                <p className="profile-danger-desc">Sign out of your account on this device.</p>
              </div>
              <button
                type="button"
                onClick={handleLogout}
                className="profile-logout-btn"
              >
                <LogOut size={15} />
                <span>Sign out</span>
              </button>
            </div>
          </section>
        </div>
      </main>
    </div>
  )
}
