import { useQuery } from '@tanstack/react-query'
import { ArrowRight, CalendarDays, CheckCircle2, Clock3, MapPin, Play, ShieldCheck, Sparkles, Ticket } from 'lucide-react'
import { useState } from 'react'
import { Link, useOutletContext } from 'react-router-dom'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { CinemaCard } from '../../components/cinema/CinemaCard'
import { ShowtimeGroups } from '../../components/cinema/ShowtimeGroups'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Modal } from '../../components/common/Overlay'
import { Reveal, StaggerGroup, StaggerItem } from '../../components/common/Motion'
import { SafeImage } from '../../components/common/SafeImage'
import { MovieGrid } from '../../components/movie/MovieGrid'
import { getApiError } from '../../utils/apiError'
import { dateToApi } from '../../utils/formatters'
import { getYoutubeEmbedUrl } from '../../utils/youtube'

export function HomePage() {
  const [trailerOpen, setTrailerOpen] = useState(false)
  const { selectedCinemaId = '', selectedCinema } = useOutletContext() || {}
  const now = useQuery({ queryKey: ['movies', 'NOW_SHOWING'], queryFn: movieApi.nowShowing, staleTime: 120000 })
  const coming = useQuery({ queryKey: ['movies', 'COMING_SOON'], queryFn: movieApi.comingSoon, staleTime: 120000 })
  const cinemas = useQuery({ queryKey: ['cinemas', 'ACTIVE'], queryFn: () => cinemaApi.list('ACTIVE'), staleTime: 300000 })
  const showtimes = useQuery({ queryKey: ['showtimes', 'date', dateToApi()], queryFn: () => showtimeApi.byDate(dateToApi()), staleTime: 60000 })
  const featured = now.data?.[0]
  const trailer = getYoutubeEmbedUrl(featured?.trailerUrl)
  const genres = (featured?.genres || []).map((item) => item.name).filter(Boolean).slice(0, 3).join(' · ')
  const filteredShowtimes = (showtimes.data || []).filter((item) => !selectedCinemaId || String(item.cinemaId) === String(selectedCinemaId))

  return <>
    <section className={`hero ${featured?.posterUrl ? 'has-poster' : ''}`}>
      {featured?.posterUrl && <SafeImage src={featured.posterUrl} alt="" className="hero-backdrop" loading="eager" aria-hidden="true" />}
      <div className="hero-overlay" />
      <div className="hero-grain" />
      <div className="container hero-grid">
        <div className="hero-copy">
          <span className="hero-kicker"><Sparkles /> {featured ? 'Tâm điểm màn ảnh' : 'Điện ảnh trong tầm tay'}</span>
          <h1>{featured?.title || <>Mỗi suất chiếu.<br /><em>Một thế giới mới.</em></>}</h1>
          {featured ? <><div className="hero-meta"><span className="age-rating">{featured.ageRating || 'P'}</span><span><Clock3 /> {featured.durationMinutes || '—'} phút</span>{genres && <span>{genres}</span>}</div><p>{featured.description || 'Nội dung phim đang được cập nhật.'}</p></> : <p>Khám phá lịch chiếu thật, chọn ghế trực quan và thanh toán an toàn qua hệ thống MovieTicket.</p>}
          <div className="hero-actions">
            <Link className="button button-primary button-lg" to={featured ? `/movies/${featured.id}#showtimes` : '/movies'}><Ticket /> Mua vé ngay</Link>
            {trailer && <button className="button button-ghost button-lg" type="button" onClick={() => setTrailerOpen(true)}><Play /> Xem trailer</button>}
          </div>
          <div className="hero-trust"><span><CheckCircle2 /> Dữ liệu trực tiếp</span><span><ShieldCheck /> Thanh toán xác minh</span></div>
        </div>
        {featured && <Reveal className="hero-poster-wrap" delay={0.12}><SafeImage src={featured.posterUrl} alt={`Poster ${featured.title}`} className="hero-poster" loading="eager" /><span className="poster-reflection" /></Reveal>}
      </div>
      <div className="scroll-cue"><span>Khám phá</span><i /></div>
    </section>

    <Modal open={trailerOpen} onClose={() => setTrailerOpen(false)} title={featured ? `Trailer · ${featured.title}` : 'Trailer'} size="xl">
      {trailer ? <div className="video-frame"><iframe src={`${trailer}?autoplay=1`} title={`Trailer ${featured?.title || ''}`} allow="autoplay; encrypted-media; picture-in-picture" allowFullScreen /></div> : <EmptyState title="Trailer không khả dụng" message="Đường dẫn trailer chưa hợp lệ hoặc chưa được cập nhật." />}
    </Modal>

    <Reveal as="section" className="page-section container"><div className="section-heading"><div><span className="eyebrow">Trên màn ảnh rộng</span><h2>Phim đang chiếu</h2><p>Những câu chuyện đang làm nên nhịp đập tại rạp.</p></div><Link className="text-link" to="/movies?status=NOW_SHOWING">Xem tất cả <ArrowRight /></Link></div>{now.isError ? <ErrorState message={getApiError(now.error)} onRetry={now.refetch} /> : <MovieGrid movies={(now.data || []).slice(0, 8)} isLoading={now.isLoading} emptyTitle="Chưa có phim đang chiếu" />}</Reveal>

    <section className="page-section soft-section"><Reveal className="container"><div className="section-heading"><div><span className="eyebrow">Sắp ra mắt</span><h2>Hẹn bạn trên màn ảnh lớn</h2><p>Lưu lại ngày khởi chiếu cho những bộ phim đáng mong chờ.</p></div><Link className="text-link" to="/movies?status=COMING_SOON">Xem tất cả <ArrowRight /></Link></div>{coming.isError ? <ErrorState message={getApiError(coming.error)} onRetry={coming.refetch} /> : <MovieGrid movies={(coming.data || []).slice(0, 4)} isLoading={coming.isLoading} emptyTitle="Chưa có phim sắp chiếu" />}</Reveal></section>

    <Reveal as="section" className="page-section container"><div className="section-heading"><div><span className="eyebrow"><CalendarDays /> Hôm nay · {new Intl.DateTimeFormat('vi-VN', { dateStyle: 'long' }).format(new Date())}</span><h2>Lịch chiếu nổi bật</h2><p>{selectedCinemaId ? `Các suất đang mở tại ${selectedCinema?.name || 'rạp đã chọn'}.` : 'Chọn khung giờ phù hợp và bắt đầu giữ ghế.'}</p></div><Link className="text-link" to="/showtimes">Toàn bộ lịch chiếu <ArrowRight /></Link></div>{showtimes.isLoading || cinemas.isLoading ? <SkeletonGrid count={3} compact /> : showtimes.isError || cinemas.isError ? <ErrorState message={getApiError(showtimes.error || cinemas.error)} onRetry={() => { showtimes.refetch(); cinemas.refetch() }} /> : <ShowtimeGroups showtimes={filteredShowtimes.slice(0, 12)} cinemas={cinemas.data || []} />}</Reveal>

    <section className="page-section cinema-showcase"><div className="container"><div className="section-heading"><div><span className="eyebrow"><MapPin /> Điểm đến điện ảnh</span><h2>Hệ thống rạp MovieTicket</h2><p>Không gian hiện đại cho từng khoảnh khắc trên màn ảnh rộng.</p></div><Link className="text-link" to="/cinemas">Khám phá hệ thống rạp <ArrowRight /></Link></div>{cinemas.isLoading ? <SkeletonGrid count={3} compact /> : cinemas.isError ? <ErrorState message={getApiError(cinemas.error)} /> : !(cinemas.data || []).length ? <EmptyState title="Chưa có rạp hoạt động" /> : <StaggerGroup className="cinema-grid">{cinemas.data.slice(0, 3).map((cinema, index) => <StaggerItem key={cinema.id}><CinemaCard cinema={cinema} index={index} /></StaggerItem>)}</StaggerGroup>}</div></section>

    <section className="cta-section"><Reveal className="container cta-card"><div><span className="eyebrow">Đèn sắp tắt, phim sắp bắt đầu</span><h2>Sẵn sàng cho suất chiếu tiếp theo?</h2><p>Chọn bộ phim bạn yêu thích và giữ tối đa 8 ghế trong vài phút.</p></div><Link className="button button-primary button-lg" to="/showtimes">Chọn lịch chiếu <ArrowRight /></Link></Reveal></section>
  </>
}
