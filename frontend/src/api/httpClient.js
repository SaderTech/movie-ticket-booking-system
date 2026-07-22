import axios from 'axios'
import { appConfig } from '../config/appConfig'
import { clearAuthSession, getAccessToken, getRefreshToken, updateAuthSession } from '../utils/authStorage'

export const httpClient = axios.create({
  baseURL: appConfig.apiBaseUrl,
  timeout: 20000,
  headers: { 'Content-Type': 'application/json' },
})

const refreshClient = axios.create({ baseURL: appConfig.apiBaseUrl, timeout: 20000 })
let refreshPromise = null

httpClient.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

function redirectToLogin() {
  const current = `${window.location.pathname}${window.location.search}`
  const target = current.startsWith('/login') ? '/login' : `/login?redirect=${encodeURIComponent(current)}`
  window.location.assign(target)
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) throw new Error('Missing refresh token')
  const { data } = await refreshClient.post('/api/auth/refresh', null, { params: { refreshToken } })
  updateAuthSession(data)
  return data.accessToken
}

httpClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config
    if (error.response?.status !== 401 || !original || original._retry || original.skipAuthRefresh) {
      return Promise.reject(error)
    }

    original._retry = true
    try {
      if (!refreshPromise) refreshPromise = refreshAccessToken().finally(() => { refreshPromise = null })
      const accessToken = await refreshPromise
      original.headers = original.headers || {}
      original.headers.Authorization = `Bearer ${accessToken}`
      return httpClient(original)
    } catch (refreshError) {
      clearAuthSession()
      redirectToLogin()
      return Promise.reject(refreshError)
    }
  },
)

export const unwrapBooking = (response) => response?.data?.data
