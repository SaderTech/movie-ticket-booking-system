import { Check } from 'lucide-react'

const steps = ['Chọn suất', 'Chọn ghế', 'Thanh toán', 'Hoàn tất']

export function BookingProgress({ current = 1 }) {
  return <nav className="booking-progress" aria-label="Tiến trình đặt vé">{steps.map((step, index) => { const number = index + 1; const complete = number < current; const active = number === current; return <div className={`${complete ? 'complete' : ''} ${active ? 'active' : ''}`} key={step}><span>{complete ? <Check /> : number}</span><strong>{step}</strong></div> })}</nav>
}
