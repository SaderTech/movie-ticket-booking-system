import { appConfig } from '../config/appConfig'

export const formatCurrency = (value) =>
  new Intl.NumberFormat(appConfig.locale, { style: 'currency', currency: appConfig.currency }).format(Number(value || 0))

export function formatDate(value, fallback = 'Chưa cập nhật') {
  if (!value) return fallback
  const date = new Date(String(value).length === 10 ? `${value}T00:00:00` : value)
  return Number.isNaN(date.getTime()) ? fallback : new Intl.DateTimeFormat(appConfig.locale).format(date)
}

export function formatDateTime(value, fallback = 'Chưa cập nhật') {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? fallback
    : new Intl.DateTimeFormat(appConfig.locale, { dateStyle: 'short', timeStyle: 'short' }).format(date)
}

export function formatTime(value, fallback = '--:--') {
  if (!value) return fallback
  return String(value).slice(0, 5)
}

export function dateToApi(date = new Date()) {
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60000)
  return local.toISOString().slice(0, 10)
}

export const toNumber = (value) => (value === '' || value == null ? null : Number(value))
export const displayValue = (value, fallback = 'Chưa cập nhật') => (value == null || value === '' ? fallback : value)
