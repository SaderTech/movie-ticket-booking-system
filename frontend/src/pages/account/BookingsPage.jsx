import { useQuery } from '@tanstack/react-query'
import { CalendarClock, ChevronRight, Ticket } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Pagination } from '../../components/common/Pagination'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { formatCurrency, formatDateTime } from '../../utils/formatters'

export function BookingsPage() {
  const [page, setPage] = useState(0)
  const query = useQuery({ queryKey: ['bookings', page], queryFn: () => bookingApi.myBookings({ page, size: 10 }) })
  const items = query.data?.items || []
  return <div><div className="page-title compact"><span className="eyebrow"><Ticket /> Vé của tôi</span><h1>Lịch sử đặt vé</h1><p>Theo dõi trạng thái, thanh toán và vé điện tử.</p></div>{query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !items.length ? <EmptyState title="Bạn chưa có booking" message="Chọn phim và suất chiếu để bắt đầu đặt vé." action={<Link className="button button-primary" to="/movies">Khám phá phim</Link>} /> : <div className="booking-list">{items.map((booking) => <Link to={`/account/bookings/${booking.bookingCode}`} className="booking-row" key={booking.bookingCode}><div className="booking-row-icon"><Ticket /></div><div><span className="eyebrow">{booking.bookingCode}</span><strong>{(booking.seats || []).map((seat) => seat.seatCode).join(', ') || `Suất chiếu #${booking.showtimeId}`}</strong><small><CalendarClock /> {formatDateTime(booking.createdAt)}</small></div><div><strong className="price-text">{formatCurrency(booking.totalAmount)}</strong><StatusBadge value={booking.status} /></div><ChevronRight /></Link>)}<Pagination page={query.data.page ?? page} totalPages={query.data.totalPages || 1} onPageChange={setPage} /></div>}</div>
}
