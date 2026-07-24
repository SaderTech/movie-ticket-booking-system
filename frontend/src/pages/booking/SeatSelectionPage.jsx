import { useMutation, useQuery } from '@tanstack/react-query'
import { Armchair, Clock, MapPin, Ticket } from 'lucide-react'
import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { useNavigate, useParams } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { BookingProgress } from '../../components/booking/BookingProgress'
import { SeatMap } from '../../components/booking/SeatMap'
import { LoadingSpinner } from '../../components/common/DesignSystem'
import { ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { SafeImage } from '../../components/common/SafeImage'
import { appConfig, checkoutStorageKey } from '../../config/appConfig'
import { getApiError, getApiErrorCode } from '../../utils/apiError'
import { formatCurrency, formatDate, formatTime } from '../../utils/formatters'

export function SeatSelectionPage() {
  const { showtimeId } = useParams()
  const navigate = useNavigate()
  const [selected, setSelected] = useState([])
  useEffect(() => { setSelected([]) }, [showtimeId])
  const showtime = useQuery({ queryKey: ['showtime', showtimeId], queryFn: () => showtimeApi.get(showtimeId) })
  const hall = useQuery({ queryKey: ['hall', showtime.data?.roomId], queryFn: () => cinemaApi.hall(showtime.data.roomId), enabled: Boolean(showtime.data?.roomId) })
  const seats = useQuery({ queryKey: ['seats', showtimeId, showtime.data?.roomId], queryFn: () => cinemaApi.seats(showtime.data.roomId), enabled: Boolean(showtime.data?.roomId) })
  const availability = useQuery({ queryKey: ['seat-availability', showtimeId], queryFn: () => bookingApi.seatAvailability(showtimeId), refetchInterval: 15000 })
  const movie = useQuery({ queryKey: ['movie', showtime.data?.movieId], queryFn: () => movieApi.get(showtime.data.movieId), enabled: Boolean(showtime.data?.movieId) })
  const cinema = useQuery({ queryKey: ['cinema', showtime.data?.cinemaId], queryFn: () => cinemaApi.get(showtime.data.cinemaId), enabled: Boolean(showtime.data?.cinemaId) })

  const toggleSeat = (seat) => {
    const code = `${seat.rowName}${seat.seatNumber}`
    setSelected((current) => {
      if (current.includes(code)) return current.filter((item) => item !== code)
      if (current.length >= appConfig.maxSeatsPerHold) {
        toast.error(`Chỉ được chọn tối đa ${appConfig.maxSeatsPerHold} ghế`)
        return current
      }
      return [...current, code]
    })
  }

  const hold = useMutation({
    mutationFn: () => bookingApi.holdSeats({ showtimeId: Number(showtimeId), seatCodes: selected }, crypto.randomUUID()),
    onSuccess: (result) => {
      const checkout = { hold: result, showtime: showtime.data, movie: movie.data || null, cinema: cinema.data || null, hall: hall.data || null, seatCodes: selected }
      sessionStorage.setItem(checkoutStorageKey, JSON.stringify(checkout))
      navigate('/booking/checkout', { state: checkout })
    },
    onError: (error) => {
      const code = getApiErrorCode(error)
      if (['SEAT_ALREADY_HELD', 'SEAT_UNAVAILABLE', 'SEAT_ALREADY_BOOKED'].includes(code)) {
        toast.error('Một hoặc nhiều ghế vừa được người khác chọn. Vui lòng chọn lại.')
        setSelected([])
        seats.refetch()
        availability.refetch()
      } else toast.error(getApiError(error))
    },
  })

  const loading = showtime.isLoading || hall.isLoading || seats.isLoading || availability.isLoading
  const error = showtime.error || hall.error || seats.error || availability.error
  if (loading) return <div className="booking-page"><div className="container page-section"><BookingProgress current={2} /><SkeletonGrid count={3} compact /></div></div>
  if (error) return <div className="booking-page"><div className="container page-section"><ErrorState message={getApiError(error)} onRetry={() => { showtime.refetch(); hall.refetch(); seats.refetch(); availability.refetch() }} /></div></div>

  const held = new Set(availability.data?.heldSeatCodes || [])
  const booked = new Set(availability.data?.bookedSeatCodes || [])
  const displayedSeats = (seats.data || []).map((seat) => {
    const code = `${seat.rowName}${seat.seatNumber}`
    return booked.has(code) ? { ...seat, bookingStatus: 'BOOKED' } : held.has(code) ? { ...seat, bookingStatus: 'HELD' } : seat
  })
  const estimate = Number(showtime.data.price || 0) * selected.length
  const returnToMovie = () => navigate(`/movies/${showtime.data.movieId}#showtimes`)

  return <div className="booking-page"><div className="container">
    <BookingProgress current={2} onReturnToMovie={returnToMovie} onStepClick={(step) => { if (step === 1) returnToMovie() }} />
    <header className="booking-heading"><div><span className="eyebrow">Bước 2 · Chọn vị trí đẹp nhất</span><h1>Chọn ghế</h1></div><div className="booking-context"><SafeImage src={movie.data?.posterUrl} alt={`Poster ${movie.data?.title || ''}`} /><div><strong><Ticket /> {movie.data?.title || `Phim #${showtime.data.movieId}`}</strong><span><MapPin /> {cinema.data?.name || hall.data?.cinemaName || `Rạp #${showtime.data.cinemaId}`} · {hall.data?.name || `Phòng ${showtime.data.roomId}`}</span><span><Clock /> {formatDate(showtime.data.showDate)} · {formatTime(showtime.data.startTime)}</span></div></div></header>
    <div className="seat-layout"><section className="panel seat-panel"><div className="seat-panel-heading"><div><span className="eyebrow"><Armchair /> Sơ đồ phòng chiếu</span><h2>Chọn tối đa {appConfig.maxSeatsPerHold} ghế</h2></div><span className="badge neutral">{hall.data?.capacity || displayedSeats.length || 0} ghế</span></div><SeatMap key={showtimeId} seats={displayedSeats} selected={selected} onToggle={toggleSeat} /></section><aside className="panel booking-summary"><span className="eyebrow"><Armchair /> Ghế đã chọn</span><h2>{selected.length ? selected.join(', ') : 'Chưa chọn ghế'}</h2><div className="summary-row"><span>Số lượng</span><strong>{selected.length} ghế</strong></div><div className="summary-row"><span>Giá suất chiếu</span><strong>{formatCurrency(showtime.data.price)} / ghế</strong></div><div className="summary-total"><span>Tạm tính</span><strong>{formatCurrency(estimate)}</strong></div><button className="button button-primary button-lg full" disabled={!selected.length || hold.isPending} onClick={() => hold.mutate()}>{hold.isPending ? <><LoadingSpinner /> Đang giữ ghế…</> : <>Tiếp tục với {selected.length} ghế <Ticket /></>}</button></aside></div>
  </div></div>
}
