import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Calendar, Clock, Play, Star, Ticket, UsersRound } from 'lucide-react'
import toast from 'react-hot-toast'
import { useOutletContext, useParams } from 'react-router-dom'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { SafeImage } from '../../components/common/SafeImage'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { DateSelector, SectionTitle } from '../../components/common/DesignSystem'
import { Modal } from '../../components/common/Overlay'
import { Reveal } from '../../components/common/Motion'
import { ShowtimeGroups } from '../../components/cinema/ShowtimeGroups'
import { dateToApi, formatDate } from '../../utils/formatters'
import { getYoutubeEmbedUrl } from '../../utils/youtube'
import { getApiError } from '../../utils/apiError'

function nextDates() {
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date()
    date.setDate(date.getDate() + index)
    return { value: dateToApi(date), weekday: index === 0 ? 'Hôm nay' : new Intl.DateTimeFormat('vi-VN', { weekday: 'short' }).format(date), day: date.getDate(), month: `Tháng ${date.getMonth() + 1}` }
  })
}

export function MovieDetailPage() {
  const { id } = useParams()
  const { selectedCinemaId = '', selectedCinema } = useOutletContext() || {}
  const dates = useMemo(nextDates, [])
  const [date, setDate] = useState(dates[0].value)
  const [trailerOpen, setTrailerOpen] = useState(false)
  const movie = useQuery({ queryKey: ['movie', id], queryFn: () => movieApi.get(id) })
  const cinemas = useQuery({ queryKey: ['cinemas'], queryFn: () => cinemaApi.list(), staleTime: 300000 })
  const showtimes = useQuery({ queryKey: ['showtimes', 'movie', id, date], queryFn: () => showtimeApi.byMovieDate(id, date), enabled: Boolean(id) })
  if (movie.isLoading) return <div className="container page-section"><SkeletonGrid count={2} /></div>
  if (movie.isError) return <div className="container page-section"><ErrorState message={getApiError(movie.error)} onRetry={movie.refetch} /></div>
  const data = movie.data
  const embed = getYoutubeEmbedUrl(data.trailerUrl)
  const genres = (data.genres || []).map((item) => item.name).join(', ') || 'Đang cập nhật'
  const directors = (data.directors || []).map((person) => person.name).join(', ') || 'Đang cập nhật'
  const actors = (data.actors || []).map((person) => person.roleName ? `${person.name} (${person.roleName})` : person.name).join(', ') || 'Đang cập nhật'
  const filteredShowtimes = (showtimes.data || []).filter((item) => !selectedCinemaId || String(item.cinemaId) === String(selectedCinemaId))
  const openTrailer = () => embed ? setTrailerOpen(true) : toast.error('Trailer chưa có hoặc đường dẫn YouTube không hợp lệ.')
  return <div className="movie-detail-page">
    <section className="movie-detail-hero">
      <SafeImage className="movie-backdrop" src={data.posterUrl} alt="" loading="eager" aria-hidden="true" />
      <div className="movie-backdrop-overlay" />
      <div className="container movie-detail-grid"><Reveal className="detail-poster-shell"><SafeImage className="detail-poster" src={data.posterUrl} alt={`Poster ${data.title}`} loading="eager" /><span className="poster-glow" /></Reveal><div className="movie-detail-copy"><div className="movie-detail-badges"><span className="age-badge inline">{data.ageRating || 'P'}</span><span className="status-dot">{data.status === 'NOW_SHOWING' ? 'Đang chiếu' : data.status === 'COMING_SOON' ? 'Sắp chiếu' : 'Đã kết thúc'}</span></div><h1>{data.title}</h1><div className="detail-chips"><span><Clock /> {data.durationMinutes || '—'} phút</span><span><Calendar /> {formatDate(data.releaseDate)}</span><span><Star /> {genres}</span></div><p className="lead">{data.description || 'Nội dung phim đang được cập nhật.'}</p><dl className="movie-credits"><div><dt>Đạo diễn</dt><dd>{directors}</dd></div><div><dt>Diễn viên</dt><dd>{actors}</dd></div></dl><div className="detail-actions"><a href="#showtimes" className="button button-primary button-lg"><Ticket /> Chọn suất chiếu</a><button type="button" className="button button-ghost button-lg" onClick={openTrailer}><Play /> Xem trailer</button></div></div></div>
    </section>
    <Modal open={trailerOpen} onClose={() => setTrailerOpen(false)} title={`Trailer · ${data.title}`} size="xl">{embed ? <div className="video-frame"><iframe src={`${embed}?autoplay=1`} title={`Trailer ${data.title}`} allow="autoplay; encrypted-media; picture-in-picture" allowFullScreen /></div> : <EmptyState title="Trailer không khả dụng" />}</Modal>
    <section id="showtimes" className="page-section showtime-section"><div className="container"><SectionTitle eyebrow={<><UsersRound /> Chọn rạp & giờ chiếu</>} title="Lịch chiếu" description={selectedCinemaId ? `Chỉ hiển thị lịch tại ${selectedCinema?.name || 'rạp đã chọn'}.` : 'Chọn ngày để xem các khung giờ đang mở bán.'} /><DateSelector dates={dates} value={date} onChange={setDate} /><div className="showtime-results" key={`${date}-${selectedCinemaId}`}>{showtimes.isLoading || cinemas.isLoading ? <SkeletonGrid count={3} compact /> : showtimes.isError || cinemas.isError ? <ErrorState message={getApiError(showtimes.error || cinemas.error)} onRetry={() => { showtimes.refetch(); cinemas.refetch() }} /> : <ShowtimeGroups showtimes={filteredShowtimes} cinemas={cinemas.data || []} />}</div></div></section>
  </div>
}
