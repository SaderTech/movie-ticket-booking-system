import { httpClient, unwrapBooking } from './httpClient'

export const bookingApi = {
  seatAvailability: (showtimeId) => httpClient.get(`/api/bookings/showtimes/${showtimeId}/seat-availability`).then(unwrapBooking),
  holdSeats: (payload, idempotencyKey) => httpClient.post('/api/bookings/hold-seats', payload, { headers: { 'Idempotency-Key': idempotencyKey } }).then(unwrapBooking),
  releaseHold: (holdToken) => httpClient.post(`/api/bookings/holds/${holdToken}/release`).then(unwrapBooking),
  confirm: (payload, idempotencyKey) => httpClient.post('/api/bookings/confirm', payload, { headers: { 'Idempotency-Key': idempotencyKey } }).then(unwrapBooking),
  vnpayReturn: (params) => httpClient.get('/api/bookings/vnpay-return', { params }).then(unwrapBooking),
  myBookings: (params) => httpClient.get('/api/bookings/my-bookings', { params }).then(unwrapBooking),
  get: (bookingCode) => httpClient.get(`/api/bookings/${bookingCode}`).then(unwrapBooking),
  cancel: (bookingCode, reason, idempotencyKey) => httpClient.post(`/api/bookings/${bookingCode}/cancel`, { reason }, { headers: { 'Idempotency-Key': idempotencyKey } }).then(unwrapBooking),
  settings: () => httpClient.get('/api/admin/booking-settings').then(unwrapBooking),
  setting: (key) => httpClient.get(`/api/admin/booking-settings/${key}`).then(unwrapBooking),
  updateSetting: (key, settingValue) => httpClient.put(`/api/admin/booking-settings/${key}`, { settingValue }).then(unwrapBooking),
}
