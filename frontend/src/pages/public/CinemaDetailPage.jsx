import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Armchair, ExternalLink, MapPin, Phone, Projector, Theater } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { cinemaApi } from '../../api/cinemaApi'
import { showtimeApi } from '../../api/showtimeApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { DateSelector, SectionTitle } from '../../components/common/DesignSystem'
import { Reveal, StaggerGroup, StaggerItem } from '../../components/common/Motion'
import { ShowtimeGroups } from '../../components/cinema/ShowtimeGroups'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { dateToApi } from '../../utils/formatters'

function dateOptions() {
  return Array.from({ length: 7 }, (_, index) => { const date = new Date(); date.setDate(date.getDate() + index); return { value: dateToApi(date), weekday: index === 0 ? 'Hôm nay' : new Intl.DateTimeFormat('vi-VN', { weekday: 'short' }).format(date), day: date.getDate(), month: `Tháng ${date.getMonth() + 1}` } })
}

export function CinemaDetailPage() {
  const { id } = useParams()
  const dates = useMemo(dateOptions, [])
  const [date, setDate] = useState(dates[0].value)
  const cinema = useQuery({ queryKey: ['cinema', id], queryFn: () => cinemaApi.get(id) })
  const halls = useQuery({ queryKey: ['halls', id], queryFn: () => cinemaApi.halls(id) })
  const showtimes = useQuery({ queryKey: ['showtimes', 'cinema', id], queryFn: () => showtimeApi.byCinema(id) })
  if (cinema.isLoading) return <div className="container page-section"><SkeletonGrid count={2} /></div>
  if (cinema.isError) return <div className="container page-section"><ErrorState message={getApiError(cinema.error)} onRetry={cinema.refetch} /></div>
  const data = cinema.data
  const mapsUrl = data.latitude != null && data.longitude != null ? `https://www.google.com/maps?q=${data.latitude},${data.longitude}` : `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(`${data.name} ${data.address}`)}`
  const hallNames = Object.fromEntries((halls.data || []).map((hall) => [hall.id, hall.name]))
  const dayShowtimes = (showtimes.data || []).filter((item) => item.showDate === date)
  return <div className="cinema-detail-page"><section className="cinema-banner"><div className="cinema-beams" aria-hidden="true" /><div className="container cinema-detail-heading"><div className="cinema-icon large"><Projector /></div><div><span className="eyebrow">Rạp MovieTicket</span><h1>{data.name}</h1><p><MapPin /> {data.address}, {data.city}</p><p><Phone /> {data.contactPhone || 'Chưa cập nhật'}</p></div><div><StatusBadge value={data.status} /><a className="button button-ghost button-sm" href={mapsUrl} target="_blank" rel="noreferrer">Mở bản đồ <ExternalLink /></a></div></div></section><section className="container page-section"><Reveal><SectionTitle eyebrow={<><Armchair /> Cơ sở vật chất</>} title="Phòng chiếu" description="Thông tin sức chứa và trạng thái được cập nhật trực tiếp từ hệ thống." />{halls.isLoading ? <SkeletonGrid count={3} compact /> : halls.isError ? <ErrorState message={getApiError(halls.error)} /> : !(halls.data || []).length ? <EmptyState title="Chưa có phòng chiếu" /> : <StaggerGroup className="stat-grid">{halls.data.map((hall) => <StaggerItem key={hall.id}><article className="stat-card"><Theater /><div><strong>{hall.name}</strong><span>{hall.hallType} · {hall.capacity} ghế</span></div><StatusBadge value={hall.status} /></article></StaggerItem>)}</StaggerGroup>}</Reveal><Reveal as="section" className="cinema-schedule"><SectionTitle eyebrow="Lịch chiếu tại rạp" title="Chọn ngày xem phim" description="Các suất đã hủy sẽ không hiển thị trong danh sách đặt vé." /><DateSelector dates={dates} value={date} onChange={setDate} />{showtimes.isLoading ? <SkeletonGrid count={3} compact /> : showtimes.isError ? <ErrorState message={getApiError(showtimes.error)} onRetry={showtimes.refetch} /> : <ShowtimeGroups showtimes={dayShowtimes} cinemas={[data]} hallNames={hallNames} />}</Reveal></section></div>
}
