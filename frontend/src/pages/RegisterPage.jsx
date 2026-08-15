import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AuthLayout, { AuthLink } from '../components/AuthLayout'

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

        <p className="auth-footer-text">
          Already have an account?{' '}
          <AuthLink to="/login">Sign in</AuthLink>
        </p>
      </form>
    </AuthLayout>
  )
}
