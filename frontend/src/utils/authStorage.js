const AUTH_KEY = 'movieticket.auth'

export function getAuthSession() {
  try {
    const value = localStorage.getItem(AUTH_KEY)
    return value ? JSON.parse(value) : null
  } catch {
    return null
  }
}

export function setAuthSession(session) {
  localStorage.setItem(AUTH_KEY, JSON.stringify(session))
}

export function updateAuthSession(partial) {
  const current = getAuthSession() || {}
  const next = { ...current, ...partial }
  setAuthSession(next)
  return next
}

export function clearAuthSession() {
  localStorage.removeItem(AUTH_KEY)
}

export function getAccessToken() {
  return getAuthSession()?.accessToken || null
}

export function getRefreshToken() {
  return getAuthSession()?.refreshToken || null
}
