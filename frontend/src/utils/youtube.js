export function getYoutubeEmbedUrl(value) {
  if (!value) return null
  try {
    const url = new URL(value)
    const allowedHosts = ['youtube.com', 'www.youtube.com', 'm.youtube.com', 'youtu.be']
    if (!allowedHosts.includes(url.hostname.toLowerCase())) return null
    let id = url.hostname === 'youtu.be' ? url.pathname.slice(1) : url.searchParams.get('v')
    if (!id && url.pathname.startsWith('/embed/')) id = url.pathname.split('/')[2]
    return /^[A-Za-z0-9_-]{6,20}$/.test(id || '') ? `https://www.youtube-nocookie.com/embed/${id}` : null
  } catch {
    return null
  }
}
