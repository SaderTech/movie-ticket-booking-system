import { httpClient } from './httpClient'

const resourceApi = (path) => ({
  list: () => httpClient.get(path).then((r) => r.data),
  get: (id) => httpClient.get(`${path}/${id}`).then((r) => r.data),
  create: (payload) => httpClient.post(path, payload).then((r) => r.data),
  update: (id, payload) => httpClient.put(`${path}/${id}`, payload).then((r) => r.data),
})

export const movieApi = {
  list: (status) => httpClient.get('/api/movies', { params: status ? { status } : undefined }).then((r) => r.data),
  search: (keyword) => httpClient.get('/api/movies/search', { params: { keyword } }).then((r) => r.data),
  nowShowing: () => httpClient.get('/api/movies/now-showing').then((r) => r.data),
  comingSoon: () => httpClient.get('/api/movies/coming-soon').then((r) => r.data),
  get: (id) => httpClient.get(`/api/movies/${id}`).then((r) => r.data),
  create: (payload) => httpClient.post('/api/movies', payload).then((r) => r.data),
  update: (id, payload) => httpClient.put(`/api/movies/${id}`, payload).then((r) => r.data),
  start: (id) => httpClient.patch(`/api/movies/${id}/start`).then((r) => r.data),
  end: (id) => httpClient.patch(`/api/movies/${id}/end`).then((r) => r.data),
  genres: resourceApi('/api/genres'),
  actors: resourceApi('/api/actors'),
  directors: resourceApi('/api/directors'),
}
