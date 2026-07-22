import { useQuery } from '@tanstack/react-query'
import { CalendarDays, Film } from 'lucide-react'
import { useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { ShowtimeGroups } from '../../components/cinema/ShowtimeGroups'
import { DateSelector, PageHero, SectionTitle } from '../../components/common/DesignSystem'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Reveal } from '../../components/common/Motion'
import { SafeImage } from '../../components/common/SafeImage'
import { getApiError } from '../../utils/apiError'
import { dateToApi } from '../../utils/formatters'

function dateOptions() {
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() + index)
    return { value: dateToApi(date), weekday: index === 0 ? 'Hôm nay' : new Intl.DateTimeFormat('vi-VN', { weekday: 'short' }).format(date), day: date.getDate(), month: `Tháng ${date.getMonth() + 1}` }
  })
}

export function ShowtimesPage() {
  const dates = useMemo(dateOptions, [])
  const [date, setDate] = useState(dates[0].value)
  const { selectedCinemaId = '', selectedCinema } = useOutletContext() || {}
  const showtimes = useQuery({ queryKey: ['showtimes', 'date', date], queryFn: () => showtimeApi.byDate(date), staleTime: 60000 })
  const movies = useQuery({ queryKey: ['movies', 'all'], queryFn: () => movieApi.list(), staleTime: 120000 })
  const cinemas = useQuery({ queryKey: ['cinemas', 'ACTIVE'], queryFn: () => cinemaApi.list('ACTIVE'), staleTime: 300000 })
  const filtered = (showtimes.data || []).filter((item) => !selectedCinemaId || String(item.cinemaId) === String(selectedCinemaId))
  const movieGroups = (movies.data || []).map((movie) => ({ movie, showtimes: filtered.filter((item) => String(item.movieId) === String(movie.id)) })).filter((group) => group.showtimes.length)
  const loading = showtimes.isLoading || movies.isLoading || cinemas.isLoading
  const error = showtimes.error || movies.error || cinemas.error
  return <div><PageHero eyebrow={<><CalendarDays /> Lịch chiếu trực tiếp</>} title="Chọn giờ, chọn ghế, tận hưởng" description="Toàn bộ suất chiếu được tải trực tiếp từ hệ thống. Chọn một ngày để bắt đầu hành trình điện ảnh của bạn." compact><DateSelector dates={dates} value={date} onChange={setDate} /></PageHero><section className="container page-section showtimes-page"><SectionTitle eyebrow="Lịch phim" title={`Suất chiếu ${new Intl.DateTimeFormat('vi-VN', { dateStyle: 'long' }).format(new Date(`${date}T00:00:00`))}`} description={selectedCinemaId ? `Đang lọc theo ${selectedCinema?.name || 'rạp đã chọn'}.` : 'Hiển thị tại tất cả rạp đang hoạt động.'} />{loading ? <SkeletonGrid count={4} compact /> : error ? <ErrorState message={getApiError(error)} onRetry={() => { showtimes.refetch(); movies.refetch(); cinemas.refetch() }} /> : !movieGroups.length ? <EmptyState title="Chưa có suất chiếu" message="Không có lịch chiếu phù hợp trong ngày hoặc tại rạp đã chọn." /> : <div className="schedule-movie-list">{movieGroups.map(({ movie, showtimes: items }, index) => <Reveal className="schedule-movie" delay={Math.min(index * 0.04, 0.2)} key={movie.id}><div className="schedule-movie-info"><SafeImage src={movie.posterUrl} alt={`Poster ${movie.title}`} /><div><span className="age-badge inline">{movie.ageRating || 'P'}</span><h2>{movie.title}</h2><p><Film /> {movie.durationMinutes || '—'} phút · {(movie.genres || []).map((genre) => genre.name).join(', ') || 'Đang cập nhật'}</p></div></div><ShowtimeGroups showtimes={items} cinemas={cinemas.data || []} /></Reveal>)}</div>}</section></div>
}
