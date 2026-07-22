import { useQueries, useQuery } from '@tanstack/react-query'
import { Armchair, Building2, CalendarClock, Film, Theater, UsersRound } from 'lucide-react'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { userApi } from '../../api/userApi'
import { ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { getApiError } from '../../utils/apiError'

export function AdminDashboard() {
  const movies = useQuery({ queryKey: ['admin', 'movies'], queryFn: () => movieApi.list(), staleTime: 60000 })
  const cinemas = useQuery({ queryKey: ['admin', 'cinemas'], queryFn: () => cinemaApi.list(), staleTime: 60000 })
  const showtimes = useQuery({ queryKey: ['admin', 'showtimes'], queryFn: showtimeApi.list, staleTime: 60000 })
  const users = useQuery({ queryKey: ['admin', 'users', 'count'], queryFn: () => userApi.list({ page: 0, size: 1 }), staleTime: 60000 })
  const hallQueries = useQueries({ queries: (cinemas.data || []).map((cinema) => ({ queryKey: ['halls', cinema.id], queryFn: () => cinemaApi.halls(cinema.id), staleTime: 60000 })) })
  const loading = movies.isLoading || cinemas.isLoading || showtimes.isLoading || users.isLoading || hallQueries.some((query) => query.isLoading)
  const error = movies.error || cinemas.error || showtimes.error || users.error || hallQueries.find((query) => query.error)?.error
  const allMovies = movies.data || []
  const metrics = [
    ['Tổng số phim', allMovies.length, Film], ['Phim đang chiếu', allMovies.filter((item) => item.status === 'NOW_SHOWING').length, Film], ['Phim sắp chiếu', allMovies.filter((item) => item.status === 'COMING_SOON').length, Film],
    ['Tổng số rạp', (cinemas.data || []).length, Building2], ['Tổng số phòng', hallQueries.reduce((sum, query) => sum + (query.data?.length || 0), 0), Theater],
    ['Tổng suất chiếu', (showtimes.data || []).length, CalendarClock], ['Tổng người dùng', users.data?.totalElements ?? 0, UsersRound],
  ]
  return <div><div className="admin-page-title"><div><span className="eyebrow">Dữ liệu trực tiếp</span><h1>Tổng quan hệ thống</h1><p>Chỉ hiển thị chỉ số có thể lấy từ API hiện có; không có doanh thu hoặc vé giả.</p></div></div>{loading ? <SkeletonGrid count={4} compact /> : error ? <ErrorState message={getApiError(error)} /> : <div className="metric-grid">{metrics.map(([label, value, Icon]) => <article className="metric-card" key={label}><span><Icon /></span><div><strong>{new Intl.NumberFormat('vi-VN').format(value)}</strong><p>{label}</p></div></article>)}</div>}<section className="admin-note"><Armchair /><div><h2>Phạm vi dashboard</h2><p>Backend chưa có API doanh thu, thống kê toàn hệ thống hoặc danh sách tất cả booking cho admin. Các khối đó không được giả lập.</p></div></section></div>
}
