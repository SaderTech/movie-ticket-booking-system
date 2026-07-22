import { httpClient } from './httpClient'

export const showtimeApi = {
  list: () => httpClient.get('/api/showtimes').then((r) => r.data),
  get: (id) => httpClient.get(`/api/showtimes/${id}`).then((r) => r.data),
  byMovie: (movieId) => httpClient.get(`/api/showtimes/movie/${movieId}`).then((r) => r.data),
  byCinema: (cinemaId) => httpClient.get(`/api/showtimes/cinema/${cinemaId}`).then((r) => r.data),
  byDate: (date) => httpClient.get(`/api/showtimes/date/${date}`).then((r) => r.data),
  byMovieDate: (movieId, date, available = false) => httpClient.get(`/api/showtimes/movie/${movieId}/date/${date}${available ? '/available' : ''}`).then((r) => r.data),
  create: (payload) => httpClient.post('/api/showtimes', payload).then((r) => r.data),
  update: (id, payload) => httpClient.put(`/api/showtimes/${id}`, payload).then((r) => r.data),
  remove: (id) => httpClient.delete(`/api/showtimes/${id}`),
}
