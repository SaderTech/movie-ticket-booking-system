import { ArrowLeft, Check } from 'lucide-react'

const steps = ['Chọn suất', 'Chọn ghế', 'Thanh toán', 'Hoàn tất']

export function BookingProgress({ current = 1, onStepClick, onReturnToMovie, locked = false }) {
  return <div className="booking-progress-area">
    <nav className="booking-progress" aria-label="Tiến trình đặt vé">{steps.map((step, index) => {
      const number = index + 1
      const complete = number < current
      const active = number === current
      const canGoBack = complete && Boolean(onStepClick) && !locked
      return <button type="button" className={`${complete ? 'complete' : ''} ${active ? 'active' : ''} ${canGoBack ? 'can-go-back' : ''}`} key={step} onClick={() => canGoBack && onStepClick(number)} disabled={!canGoBack}><span>{complete ? <Check /> : number}</span><strong>{step}</strong></button>
    })}</nav>
  </div>
}
