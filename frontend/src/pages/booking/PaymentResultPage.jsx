import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, CircleDollarSign, History, Home, LoaderCircle, RotateCcw, XCircle } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { BookingDetails } from '../../components/booking/BookingDetails'
import { BookingProgress } from '../../components/booking/BookingProgress'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'

export function PaymentResultPage() {
  const [searchParams] = useSearchParams()
  const params = Object.fromEntries(searchParams.entries())
  const signature = searchParams.toString()
  const result = useQuery({ queryKey: ['vnpay-return', signature], queryFn: () => bookingApi.vnpayReturn(params), enabled: Boolean(signature), retry: false, staleTime: Infinity })

  if (!signature) return <PaymentOutcome message="Thiếu dữ liệu VNPay để xác minh giao dịch." code="PAYMENT_VERIFICATION_FAILED" />
  if (result.isLoading) return <div className="fullscreen-state payment-verifying"><span className="verification-orbit"><LoaderCircle className="spin" /></span><span className="eyebrow">Đang kết nối cổng thanh toán</span><h1>Đang xác minh giao dịch</h1><p>Vui lòng không đóng trang.</p></div>
  if (result.isError) return <PaymentOutcome message={getApiError(result.error)} code={result.error?.response?.data?.errorCode || 'PAYMENT_VERIFICATION_FAILED'} />

  const booking = result.data
  if (booking?.status === 'CONFIRMED' && booking?.payment?.status === 'PAID') {
    return <div className="booking-page success-page"><div className="container"><BookingProgress current={4} /><div className="success-banner"><span className="success-ring"><CheckCircle2 /></span><div><span className="eyebrow">Thanh toán đã được xác minh</span><h1>Đặt vé thành công</h1><p>Mã booking: <strong>{booking.bookingCode}</strong></p></div></div><BookingDetails booking={booking} /></div></div>
  }
  if (booking?.payment?.status === 'REFUND_PENDING') {
    return <PaymentOutcome booking={booking} refundPending message="Giao dịch được nhận sau khi phiên giữ ghế đã hết hạn. Vé không được phát hành; khoản tiền này đang chờ hoàn." code="REFUND_PENDING" />
  }
  return <PaymentOutcome booking={booking} message="Thanh toán không thành công. Ghế đã được giải phóng và bạn có thể chọn lại suất chiếu." code={booking?.payment?.status || booking?.status || 'PAYMENT_FAILED'} />
}

function PaymentOutcome({ booking, message, code, refundPending = false }) {
  const Icon = refundPending ? CircleDollarSign : XCircle
  return <div className="payment-result-page"><div className="container narrow"><BookingProgress current={3} /><div className={`payment-result-card ${refundPending ? 'refund-pending' : 'failure'}`}><span className="result-icon"><Icon /></span><span className="eyebrow">{refundPending ? 'Đang xử lý hoàn tiền' : 'Giao dịch chưa hoàn tất'}</span><h1>{refundPending ? 'Thanh toán về muộn' : 'Thanh toán chưa thành công'}</h1><p>{message}</p>{booking && <div className="payment-outcome-statuses"><StatusBadge value={booking.status} /><StatusBadge value={booking.payment?.status} /></div>}<code>{code}</code><div className="form-actions"><Link className="button button-secondary" to="/account/bookings"><History /> Lịch sử đặt vé</Link><Link className="button button-primary" to="/showtimes"><RotateCcw /> Chọn lại suất chiếu</Link><Link className="button button-ghost" to="/"><Home /> Trang chủ</Link></div></div></div></div>
}
