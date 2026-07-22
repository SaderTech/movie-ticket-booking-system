import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Ban } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { useParams } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { BookingDetails } from '../../components/booking/BookingDetails'
import { ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { LoadingSpinner } from '../../components/common/DesignSystem'
import { Modal } from '../../components/common/Overlay'
import { getApiError } from '../../utils/apiError'

export function BookingDetailPage() {
  const { bookingCode } = useParams()
  const [showCancel, setShowCancel] = useState(false)
  const [reason, setReason] = useState('')
  const queryClient = useQueryClient()
  const booking = useQuery({ queryKey: ['booking', bookingCode], queryFn: () => bookingApi.get(bookingCode) })
  const showtime = useQuery({ queryKey: ['showtime', booking.data?.showtimeId], queryFn: () => showtimeApi.get(booking.data.showtimeId), enabled: Boolean(booking.data?.showtimeId) })
  const movie = useQuery({ queryKey: ['movie', showtime.data?.movieId], queryFn: () => movieApi.get(showtime.data.movieId), enabled: Boolean(showtime.data?.movieId) })
  const cinema = useQuery({ queryKey: ['cinema', showtime.data?.cinemaId], queryFn: () => cinemaApi.get(showtime.data.cinemaId), enabled: Boolean(showtime.data?.cinemaId) })
  const cancel = useMutation({ mutationFn: () => bookingApi.cancel(bookingCode, reason.trim(), crypto.randomUUID()), onSuccess: (data) => { queryClient.setQueryData(['booking', bookingCode], data); toast.success('Đã hủy booking'); setShowCancel(false) }, onError: (error) => toast.error(getApiError(error)) })
  if (booking.isLoading) return <SkeletonGrid count={2} compact />
  if (booking.isError) return <ErrorState message={getApiError(booking.error)} onRetry={booking.refetch} />
  const canCancel = ['HOLDING', 'PENDING_PAYMENT', 'CONFIRMED'].includes(booking.data.status)
  return <div><div className="section-heading"><div><span className="eyebrow">Chi tiết booking</span><h1>{bookingCode}</h1></div>{canCancel && <button className="button button-danger" onClick={() => setShowCancel(true)}><Ban /> Hủy booking</button>}</div><BookingDetails booking={booking.data} showtime={showtime.data} movie={movie.data} cinema={cinema.data} /><Modal open={showCancel} onClose={() => setShowCancel(false)} title="Xác nhận hủy booking"><form className="modal-form" onSubmit={(event) => { event.preventDefault(); cancel.mutate() }}><p>Trạng thái hủy và khả năng hoàn tiền phụ thuộc hoàn toàn vào backend hiện tại.</p><label>Lý do hủy<textarea value={reason} onChange={(event) => setReason(event.target.value)} rows="4" placeholder="Nhập lý do (không bắt buộc)" /></label><div className="form-actions"><button type="button" className="button button-secondary" onClick={() => setShowCancel(false)}>Quay lại</button><button className="button button-danger solid" disabled={cancel.isPending}>{cancel.isPending ? <><LoadingSpinner /> Đang hủy…</> : 'Xác nhận hủy'}</button></div></form></Modal></div>
}
