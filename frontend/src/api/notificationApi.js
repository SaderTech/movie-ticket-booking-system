import { httpClient } from './httpClient'

export const notificationApi = {
  send: (payload) => httpClient.post('/api/v1/notifications/send', payload).then((r) => r.data),
  logs: () => httpClient.get('/api/v1/notifications/logs').then((r) => r.data),
  searchLogs: (params) => httpClient.get('/api/v1/notifications/logs/search', { params }).then((r) => r.data),
  getLog: (id) => httpClient.get(`/api/v1/notifications/${id}`).then((r) => r.data),
  resend: (id) => httpClient.post(`/api/v1/notifications/${id}/resend`).then((r) => r.data),
  templates: () => httpClient.get('/api/v1/notification-templates').then((r) => r.data),
  createTemplate: (payload) => httpClient.post('/api/v1/notification-templates', payload).then((r) => r.data),
  ping: () => httpClient.get('/ping').then((r) => r.data),
  qrUrl: (ticketCode) => `${httpClient.defaults.baseURL}/api/v1/qr-codes/ticket/${encodeURIComponent(ticketCode)}`,
}
