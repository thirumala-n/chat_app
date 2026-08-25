import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AuthLayout, { AuthLink } from '../components/AuthLayout'

const GOOGLE_SVG = (
  <svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true">
    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
  </svg>
)

export default function RegisterPage() {
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    username: '',
    email: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) =>
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }
    setLoading(true)
    try {
      await register(form)
      navigate('/chat')
    } catch (err) {
      const data = err.response?.data
      if (data?.fieldErrors) {
        setError(Object.values(data.fieldErrors).join(', '))
      } else {
        setError(data?.message || 'Registration failed. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Start chatting with AI in seconds">
      <form onSubmit={handleSubmit} noValidate>
        {error && <div className="auth-error" role="alert">{error}</div>}

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="reg-firstName" className="form-label">First name</label>
            <input
              id="reg-firstName"
              name="firstName"
              className="form-input"
              placeholder="Jane"
              value={form.firstName}
              onChange={handleChange}
              autoFocus
            />
          </div>
          <div className="form-group">
            <label htmlFor="reg-lastName" className="form-label">Last name</label>
            <input
              id="reg-lastName"
              name="lastName"
              className="form-input"
              placeholder="Smith"
              value={form.lastName}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="reg-username" className="form-label">Username</label>
          <input
            id="reg-username"
            name="username"
            className="form-input"
            placeholder="janesmith"
            value={form.username}
            onChange={handleChange}
            required
            autoComplete="username"
          />
        </div>

        <div className="form-group">
          <label htmlFor="reg-email" className="form-label">Email address</label>
          <input
            id="reg-email"
            name="email"
            type="email"
            className="form-input"
            placeholder="you@example.com"
            value={form.email}
            onChange={handleChange}
            required
            autoComplete="email"
          />
        </div>

        <div className="form-group">
          <label htmlFor="reg-password" className="form-label">Password</label>
          <input
            id="reg-password"
            name="password"
            type="password"
            className="form-input"
            placeholder="At least 8 characters"
            value={form.password}
            onChange={handleChange}
            required
            minLength={8}
            autoComplete="new-password"
          />
        </div>

        <button
          type="submit"
          className="btn-primary"
          disabled={loading}
          aria-busy={loading}
          style={{ marginTop: 4 }}
        >
          {loading ? 'Creating account…' : 'Create account'}
        </button>

        <div className="auth-divider">
          <div className="auth-divider-line" />
          <span className="auth-divider-text">or</span>
          <div className="auth-divider-line" />
        </div>

        <a
          href="/api/oauth2/authorization/google"
          className="btn-google"
          aria-label="Continue with Google"
        >
          {GOOGLE_SVG}
          Continue with Google
        </a>

        <p className="auth-footer-text">
          Already have an account?{' '}
          <AuthLink to="/login">Sign in</AuthLink>
        </p>
      </form>
    </AuthLayout>
  )
}
