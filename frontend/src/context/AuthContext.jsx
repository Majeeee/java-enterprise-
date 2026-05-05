import { createContext, useContext, useState, useCallback } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = sessionStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback(async (email, password) => {
    const data = await authService.login(email, password)
    setUser(data)
    sessionStorage.setItem('user', JSON.stringify(data))
    return data
  }, [])

  const register = useCallback(async (formData) => {
    return authService.register(formData)
  }, [])

  const logout = useCallback(async () => {
    await authService.logout()
    setUser(null)
    sessionStorage.removeItem('user')
  }, [])

  const isAdmin = user?.role === 'ROLE_ADMIN'

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth måste användas inuti AuthProvider')
  return ctx
}
