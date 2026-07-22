import { httpClient } from './httpClient'

export const userApi = {
  me: () => httpClient.get('/api/users/me').then((r) => r.data),
  list: (params) => httpClient.get('/api/users', { params }).then((r) => r.data),
  get: (id) => httpClient.get(`/api/users/${id}`).then((r) => r.data),
  update: (id, payload) => httpClient.put(`/api/users/${id}`, payload).then((r) => r.data),
  remove: (id) => httpClient.delete(`/api/users/${id}`),
}
