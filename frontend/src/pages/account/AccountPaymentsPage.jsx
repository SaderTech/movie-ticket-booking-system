import { useQuery } from '@tanstack/react-query'
import { CreditCard, ReceiptText, Ticket } from 'lucide-react'
import { Link } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { formatCurrency, formatDateTime } from '../../utils/formatters'

export function AccountPaymentsPage() {
  const query = useQuery({ queryKey: ['account', 'payment-history'], queryFn: () => bookingApi.myBookings({ page: 0, size: 100 }) })
  const bookings = query.data?.items || []
  const payments = bookings
    .filter((booking) => booking.payment)
    .map((booking) => ({ ...booking.payment, bookingCode: booking.bookingCode, bookingStatus: booking.status, createdAt: booking.createdAt, totalAmount: booking.totalAmount }))

  return <div>
    <div className="page-title compact"><span className="eyebrow"><CreditCard /> Thanh toán</span><h1>Lịch sử thanh toán</h1><p>Dữ liệu lấy từ booking hiện có. Không tạo DB payment riêng ở frontend nếu Booking Service đã trả payment log.</p></div>
    {query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !payments.length ? <EmptyState title="Chưa có thanh toán" message="Các giao dịch VNPay sẽ xuất hiện sau khi bạn tạo booking và thực hiện thanh toán." /> : <section className="panel table-panel"><div className="table-scroll"><table><thead><tr><th>Booking</th><th>Giao dịch</th><th>Phương thức</th><th>Số tiền</th><th>Trạng thái</th><th>Thời gian</th><th></th></tr></thead><tbody>{payments.map((payment) => <tr key={payment.id || `${payment.bookingCode}-${payment.transactionRef}`}><td><strong>{payment.bookingCode}</strong><small>Booking: {payment.bookingStatus}</small></td><td><span className="inline-icon"><ReceiptText /> {payment.transactionRef || 'Chưa có mã giao dịch'}</span></td><td>{payment.method || '—'}</td><td><strong className="price-text">{formatCurrency(payment.amount || payment.totalAmount)}</strong></td><td><StatusBadge value={payment.status} /></td><td>{formatDateTime(payment.paidAt || payment.createdAt)}</td><td><Link className="button button-ghost compact" to={`/account/bookings/${payment.bookingCode}`}><Ticket /> Xem vé</Link></td></tr>)}</tbody></table></div></section>}
  </div>
}
