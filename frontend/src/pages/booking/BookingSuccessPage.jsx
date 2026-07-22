import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, Home, Ticket } from 'lucide-react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { BookingDetails } from '../../components/booking/BookingDetails'
import { ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { getApiError } from '../../utils/apiError'
import { BookingProgress } from '../../components/booking/BookingProgress'

export function BookingSuccessPage() {
  const { bookingCode } = useParams()
  const location = useLocation()
  const query = useQuery({ queryKey: ['booking', bookingCode], queryFn: () => bookingApi.get(bookingCode), initialData: location.state?.booking })
  if (query.isLoading) return <div className="booking-page"><div className="container page-section"><SkeletonGrid count={2} compact /></div></div>
  if (query.isError) return <div className="booking-page"><div className="container page-section"><ErrorState message={getApiError(query.error)} /></div></div>
  return <div className="booking-page success-page"><div className="container"><BookingProgress current={4} /><div className="success-banner"><span className="success-ring"><CheckCircle2 /></span><div><span className="eyebrow">Hành trình đã sẵn sàng</span><h1>Đặt vé thành công</h1><p>Mã booking: <strong>{bookingCode}</strong></p></div><div className="success-actions"><Link className="button button-secondary" to="/account/bookings"><Ticket /> Xem vé của tôi</Link><Link className="button button-ghost" to="/"><Home /> Trang chủ</Link></div></div><BookingDetails booking={query.data} /></div></div>
}
