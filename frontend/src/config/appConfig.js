export const appConfig = {
  brandName: 'MovieTicket',
  apiBaseUrl: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, ''),
  frontendUrl: 'http://localhost:3000',
  locale: 'vi-VN',
  currency: 'VND',
  maxSeatsPerHold: 8,
  movieSearchDebounceMs: 500,
}

export const brandConfig = {
  name: 'MovieTicket',
  mark: 'MT',
  tagline: 'Mỗi suất chiếu, một thế giới mới.',
  description: 'Nền tảng đặt vé điện ảnh trực tuyến, kết nối bạn với những khoảnh khắc đáng nhớ trên màn ảnh rộng.',
  colors: {
    '--color-primary': '#f97316',
    '--color-primary-hover': '#fb923c',
    '--color-secondary': '#22d3ee',
    '--color-accent': '#ef4444',
  },
  socialLinks: [],
}

export const checkoutStorageKey = 'movieticket.checkout'
export const selectedCinemaKey = 'movieticket.selectedCinema'
