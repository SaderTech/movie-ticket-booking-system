import { httpClient } from './httpClient'

export const authApi = {
  register: (payload) => httpClient.post('/api/auth/register', payload, { skipAuthRefresh: true }).then((r) => r.data),
  login: (payload) => httpClient.post('/api/auth/login', payload, { skipAuthRefresh: true }).then((r) => r.data),
  refresh: (refreshToken) => httpClient.post('/api/auth/refresh', null, { params: { refreshToken }, skipAuthRefresh: true }).then((r) => r.data),
}
