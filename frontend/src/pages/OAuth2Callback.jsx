import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Sparkles } from 'lucide-react'

export default function OAuth2Callback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { handleOAuthCallback } = useAuth()

  useEffect(() => {
    const accessToken = searchParams.get('accessToken')
    const refreshToken = searchParams.get('refreshToken')

    if (!accessToken) {
      navigate('/login?error=oauth_failed', { replace: true })
      return
    }

    const finish = async () => {
      try {
        await handleOAuthCallback(accessToken, refreshToken)
        navigate('/chat', { replace: true })
      } catch (err) {
        console.error('OAuth callback failed:', err)
        navigate('/login?error=oauth_failed', { replace: true })
      }
    }

    finish()
  }, [searchParams, navigate, handleOAuthCallback])

  return (
    <div className="oauth-loading" style={{ background: 'var(--bg-base)' }}>
      <div
        style={{
          width: 48,
          height: 48,
          background: 'var(--accent)',
          borderRadius: 12,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          marginBottom: 8,
        }}
      >
        <Sparkles size={24} />
      </div>
      <div className="oauth-spinner" aria-hidden="true" />
      <p style={{ color: 'var(--text-secondary)', fontSize: 14, marginTop: 8 }}>
        Signing you in with Google…
      </p>
    </div>
  )
}