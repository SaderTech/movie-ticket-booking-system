import { createContext, useCallback, useMemo, useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { authApi } from '../api/authApi'
import { clearAuthSession, getAuthSession, setAuthSession } from '../utils/authStorage'
import { getRoles, hasAdminRole } from '../utils/jwtUtils'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => getAuthSession())
  const queryClient = useQueryClient()

  const login = useCallback(async (credentials) => {
    const data = await authApi.login(credentials)
    setAuthSession(data)
    setSession(data)
    return data
  }, [])

  const logout = useCallback(() => {
    clearAuthSession()
    setSession(null)
    queryClient.clear()
  }, [queryClient])

  const value = useMemo(() => ({
    session,
    user: session ? { id: session.userId, username: session.username, email: session.email } : null,
    roles: getRoles(session?.accessToken),
    isAuthenticated: Boolean(session?.accessToken),
    isAdmin: hasAdminRole(session?.accessToken),
    login,
    logout,
  }), [session, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
