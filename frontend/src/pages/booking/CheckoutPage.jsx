import { useMutation } from '@tanstack/react-query'
import { AlertCircle, Clock3, CreditCard, ShieldCheck, Ticket } from 'lucide-react'
import { useCallback, useEffect, useMemo, useRef } from 'react'
import toast from 'react-hot-toast'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { bookingApi } from '../../api/bookingApi'
import { BookingProgress } from '../../components/booking/BookingProgress'
import { LoadingSpinner } from '../../components/common/DesignSystem'
import { SafeImage } from '../../components/common/SafeImage'
import { appConfig, checkoutStorageKey } from '../../config/appConfig'
import { useCountdown } from '../../hooks/useCountdown'
import { getApiError } from '../../utils/apiError'
import { formatCurrency, formatDate, formatTime } from '../../utils/formatters'

function readCheckout(state) {
  if (state?.hold) return state
  try { return JSON.parse(sessionStorage.getItem(checkoutStorageKey)) } catch { return null }
}

export function CheckoutPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const checkout = useMemo(() => readCheckout(location.state), [location.state])
  const paymentStarted = useRef(false)
  const releaseStarted = useRef(false)
  const countdown = useCountdown(checkout?.hold?.expiresAt)
  const totalDuration = checkout?.hold?.createdAt
    ? new Date(checkout.hold.expiresAt).getTime() - new Date(checkout.hold.createdAt).getTime()
    : null
  const progress = totalDuration > 0
    ? Math.max(0, Math.min(100, countdown.remainingMs / totalDuration * 100))
    : null

  const releaseHold = useCallback(async () => {
    if (!checkout?.hold?.holdToken || paymentStarted.current || releaseStarted.current) return true

    releaseStarted.current = true
    try {
      await bookingApi.releaseHold(checkout.hold.holdToken)
      sessionStorage.removeItem(checkoutStorageKey)
      return true
    } catch (error) {
      releaseStarted.current = false
      toast.error(`Không thể giải phóng ghế: ${getApiError(error)}`)
      return false
    }
  }, [checkout])

  // Browser Back changes route before React unmounts. Release the hold here;
  // the backend operation is idempotent, so an explicit back click is safe too.
  useEffect(() => {
    const handleBrowserBack = () => { void releaseHold() }
    window.addEventListener('popstate', handleBrowserBack)
    return () => window.removeEventListener('popstate', handleBrowserBack)
  }, [releaseHold])

  const returnToSeats = async () => {
    if (await releaseHold()) navigate(`/booking/${checkout.showtime?.id}/seats`)
  }

  const returnToMovie = async () => {
    if (await releaseHold()) navigate(`/movies/${checkout.showtime?.movieId}#showtimes`)
  }

  const confirm = useMutation({
    mutationFn: () => bookingApi.confirm({
      holdToken: checkout.hold.holdToken,
      paymentMethod: 'VNPAY',
      returnUrl: `${appConfig.frontendUrl}/payment-result`,
    }, crypto.randomUUID()),
    onMutate: () => { paymentStarted.current = true },
    onSuccess: (booking) => {
      if (booking.paymentUrl) {
        window.location.assign(booking.paymentUrl)
        return
      }
      sessionStorage.removeItem(checkoutStorageKey)
      if (booking.bookingCode) navigate(`/booking/success/${booking.bookingCode}`, { state: { booking } })
      else toast.error('Backend không trả URL thanh toán hoặc mã booking.')
    },
    onError: (error) => {
      paymentStarted.current = false
      toast.error(getApiError(error))
    },
  })

  if (!checkout?.hold) {
    return <div className="booking-page"><div className="container page-section"><BookingProgress current={3} /><div className="state-card"><AlertCircle /><h1>Không có phiên giữ ghế</h1><p>Hãy chọn suất chiếu và ghế trước khi thanh toán.</p><Link className="button button-primary" to="/movies">Chọn phim</Link></div></div></div>
  }

  const estimate = Number(checkout.showtime?.price || 0) * (checkout.seatCodes?.length || 0)

  return <div className="booking-page"><div className="container">
    <BookingProgress current={3} onReturnToMovie={returnToMovie} onStepClick={(step) => { if (step === 1) returnToMovie(); if (step === 2) returnToSeats() }} locked={confirm.isPending} />
    <header className="booking-heading"><div><span className="eyebrow">Bước 3 · Xác nhận giao dịch</span><h1>Thanh toán</h1></div><div className={`countdown ${countdown.expired ? 'expired' : countdown.remainingMs < 120000 ? 'warning' : ''}`}><Clock3 /><div><span>Thời gian giữ ghế</span><strong>{countdown.label}</strong></div>{progress != null && <div className="countdown-progress" role="progressbar" aria-label="Thời gian giữ ghế còn lại" aria-valuemin="0" aria-valuemax="100" aria-valuenow={Math.round(progress)}><i style={{ width: `${progress}%` }} /></div>}</div></header>
    <div className="checkout-layout"><section className="panel checkout-main"><div className="checkout-movie"><SafeImage src={checkout.movie?.posterUrl} alt={`Poster ${checkout.movie?.title || ''}`} /><div><span className="eyebrow"><Ticket /> Vé của bạn</span><h2>{checkout.movie?.title || `Phim #${checkout.showtime?.movieId}`}</h2><p>{checkout.cinema?.name || checkout.hall?.cinemaName || `Rạp #${checkout.showtime?.cinemaId}`} · {checkout.hall?.name || `Phòng #${checkout.showtime?.roomId}`}</p><strong>{formatDate(checkout.showtime?.showDate)} · {formatTime(checkout.showtime?.startTime)}</strong></div></div><div className="detail-list compact"><div><span>Ghế</span><strong>{checkout.seatCodes?.join(', ')}</strong></div><div><span>Số lượng</span><strong>{checkout.seatCodes?.length} vé</strong></div></div><div><h2>Phương thức thanh toán</h2><p className="muted">Thanh toán qua cổng VNPay.</p></div><div className="payment-options"><label className="selected"><input type="radio" name="payment" value="VNPAY" checked readOnly /><span className="payment-icon"><CreditCard /></span><span><strong>VNPay Sandbox</strong><small>Thanh toán qua cổng VNPay và quay lại MovieTicket.</small></span><i /></label></div></section><aside className="panel booking-summary payment-summary"><span className="eyebrow">Tóm tắt thanh toán</span><div className="summary-row"><span>{checkout.seatCodes?.length} ghế</span><strong>{formatCurrency(estimate)}</strong></div><div className="summary-total"><span>Tổng tạm tính</span><strong>{formatCurrency(estimate)}</strong></div>{countdown.expired && <p className="error-note">Phiên giữ ghế đã hết hạn. Vui lòng chọn lại.</p>}<button className="button button-primary button-lg full" disabled={countdown.expired || confirm.isPending} onClick={() => confirm.mutate()}>{confirm.isPending ? <><LoadingSpinner /> Đang tạo thanh toán…</> : <><ShieldCheck /> Thanh toán với VNPay</>}</button><button className="button button-secondary full" disabled={confirm.isPending} onClick={returnToSeats}>Quay lại chọn ghế</button></aside></div>
  </div></div>
}
