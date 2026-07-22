import { useEffect, useState } from 'react'

export function useCountdown(expiresAt) {
  const calculate = () => Math.max(0, new Date(expiresAt || 0).getTime() - Date.now())
  const [remainingMs, setRemainingMs] = useState(calculate)

  useEffect(() => {
    setRemainingMs(calculate())
    const timer = window.setInterval(() => setRemainingMs(calculate()), 1000)
    return () => window.clearInterval(timer)
  }, [expiresAt])

  const seconds = Math.ceil(remainingMs / 1000)
  return {
    remainingMs,
    expired: remainingMs <= 0,
    label: `${String(Math.floor(seconds / 60)).padStart(2, '0')}:${String(seconds % 60).padStart(2, '0')}`,
  }
}
