import { useQuery } from '@tanstack/react-query'
import { CheckCircle2, History, Home, LoaderCircle, RotateCcw, XCircle } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { BookingDetails } from '../../components/booking/BookingDetails'
import { BookingProgress } from '../../components/booking/BookingProgress'
import { getApiError } from '../../utils/apiError'

export function PaymentResultPage() {
  const [searchParams] = useSearchParams()
  const params = Object.fromEntries(searchParams.entries())
  const signature = searchParams.toString()
  const result = useQuery({ queryKey: ['vnpay-return', signature], queryFn: () => bookingApi.vnpayReturn(params), enabled: Boolean(signature), retry: false, staleTime: Infinity })
  if (!signature) return <div className="payment-result-page"><div className="container narrow"><BookingProgress current={3} /><div className="payment-result-card failure"><span className="result-icon"><XCircle /></span><span className="eyebrow">Không thể xác minh</span><h1>Thiếu dữ liệu VNPay</h1><p>Trang không nhận được query parameters để backend kiểm tra chữ ký thanh toán.</p><div className="form-actions"><Link className="button button-secondary" to="/account/bookings"><History /> Lịch sử đặt vé</Link><Link className="button button-primary" to="/"><Home /> Trang chủ</Link></div></div></div></div>
  if (result.isLoading) return <div className="fullscreen-state payment-verifying"><span className="verification-orbit"><LoaderCircle className="spin" /></span><span className="eyebrow">Đang kết nối cổng thanh toán</span><h1>Đang xác minh giao dịch</h1><p>MovieTicket đang gửi nguyên vẹn thông tin VNPay về backend để kiểm tra chữ ký. Vui lòng không đóng trang.</p></div>
  if (result.isError) return <div className="payment-result-page"><div className="container narrow"><BookingProgress current={3} /><div className="payment-result-card failure"><span className="result-icon"><XCircle /></span><span className="eyebrow">Giao dịch chưa hoàn tất</span><h1>Thanh toán chưa thành công</h1><p>{getApiError(result.error)}</p><code>{result.error?.response?.data?.errorCode || 'PAYMENT_VERIFICATION_FAILED'}</code><div className="form-actions"><Link className="button button-secondary" to="/account/bookings"><History /> Lịch sử đặt vé</Link><Link className="button button-primary" to="/showtimes"><RotateCcw /> Thử lại</Link></div></div></div></div>
  return <div className="booking-page success-page"><div className="container"><BookingProgress current={4} /><div className="success-banner"><span className="success-ring"><CheckCircle2 /></span><div><span className="eyebrow">Thanh toán đã được xác minh</span><h1>Đặt vé thành công</h1><p>Mã booking: <strong>{result.data.bookingCode}</strong></p></div></div><BookingDetails booking={result.data} /></div></div>
}
