import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import AuthLayout, { AuthLink } from '../components/AuthLayout'

export default function RegisterPage() {
  const [form, setForm] = useState({ username: '', email: '', password: '', firstName: '', lastName: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/chat')
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.fieldErrors
        ? Object.values(err.response.data.fieldErrors).join(', ')
        : 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const inputClass = "w-full rounded-lg bg-surface-light border border-slate-600 px-4 py-2.5 text-white placeholder-slate-500 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"

  return (
    <AuthLayout title="Create account" subtitle="Get started with AI Chat Platform">
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && <div className="rounded-lg bg-red-500/10 border border-red-500/30 px-4 py-3 text-sm text-red-400">{error}</div>}
        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">First name</label>
            <input name="firstName" value={form.firstName} onChange={handleChange} className={inputClass} />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1.5">Last name</label>
            <input name="lastName" value={form.lastName} onChange={handleChange} className={inputClass} />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1.5">Username</label>
          <input name="username" value={form.username} onChange={handleChange} required className={inputClass} />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1.5">Email</label>
          <input name="email" type="email" value={form.email} onChange={handleChange} required className={inputClass} />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1.5">Password</label>
          <input name="password" type="password" value={form.password} onChange={handleChange} required minLength={8} className={inputClass} />
        </div>
        <button type="submit" disabled={loading}
          className="w-full rounded-lg bg-primary-600 py-2.5 font-medium text-white hover:bg-primary-500 disabled:opacity-50 transition-colors">
          {loading ? 'Creating account...' : 'Create account'}
        </button>
        <p className="text-center text-sm text-slate-400">
          Already have an account? <AuthLink to="/login">Sign in</AuthLink>
        </p>
      </form>
    </AuthLayout>
  )
}
