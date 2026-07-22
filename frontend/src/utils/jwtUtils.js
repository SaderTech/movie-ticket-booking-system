import { jwtDecode } from 'jwt-decode'

export function decodeToken(token) {
  if (!token) return null
  try {
    return jwtDecode(token)
  } catch {
    return null
  }
}

export function getRoles(token) {
  const claims = decodeToken(token)
  const value = claims?.roles ?? claims?.role ?? []
  if (Array.isArray(value)) return value.map(String)
  return String(value)
    .replace(/[\[\]"]/g, '')
    .split(/[\s,]+/)
    .filter(Boolean)
}

export function hasAdminRole(token) {
  return getRoles(token).some((role) => ['ADMIN', 'ROLE_ADMIN'].includes(role.toUpperCase()))
}

export function isTokenExpired(token) {
  const claims = decodeToken(token)
  return !claims?.exp || claims.exp * 1000 <= Date.now()
}
