import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi, userApi } from '../api/services'
import { clearTokens, setTokens } from '../api/config'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })
  const [loading, setLoading] = useState(true)

  const loadUser = useCallback(async () => {
    try {
      const { data } = await userApi.getProfile()
      setUser(data.data)
      localStorage.setItem('user', JSON.stringify(data.data))
    } catch {
      clearTokens()
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const token = localStorage.getItem('accessToken')
    if (token) loadUser()
    else setLoading(false)
  }, [loadUser])

  const login = async (email, password) => {
    const { data } = await authApi.login({ email, password })
    setTokens(data.data.accessToken, data.data.refreshToken)
    setUser(data.data.user)
    localStorage.setItem('user', JSON.stringify(data.data.user))
    return data.data
  }

  const register = async (formData) => {
    const { data } = await authApi.register(formData)
    setTokens(data.data.accessToken, data.data.refreshToken)
    setUser(data.data.user)
    localStorage.setItem('user', JSON.stringify(data.data.user))
    return data.data
  }

  const logout = async () => {
    try { await authApi.logout() } catch { /* ignore */ }
    clearTokens()
    setUser(null)
  }

  const updateUser = (updated) => {
    setUser(updated)
    localStorage.setItem('user', JSON.stringify(updated))
  }

  const handleOAuthCallback = (accessToken, refreshToken) => {
    setTokens(accessToken, refreshToken)
    loadUser()
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser, loadUser, handleOAuthCallback }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
