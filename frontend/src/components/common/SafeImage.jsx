import { useEffect, useState } from 'react'

export function SafeImage({ src, alt, className = '', fallback = '/poster-fallback.svg', loading = 'lazy', ...props }) {
  const [current, setCurrent] = useState(src || fallback)
  useEffect(() => setCurrent(src || fallback), [src, fallback])
  return <img {...props} className={className} src={current} alt={alt || ''} loading={loading} decoding="async" onError={() => setCurrent(fallback)} />
}
