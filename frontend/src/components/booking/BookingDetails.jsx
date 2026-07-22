import { Home, Printer, Ticket } from 'lucide-react'
import { Link } from 'react-router-dom'
import { notificationApi } from '../../api/notificationApi'
import { displayValue, formatCurrency, formatDate, formatDateTime, formatTime } from '../../utils/formatters'
import { SafeImage } from '../common/SafeImage'
import { StatusBadge } from '../common/StatusBadge'

export function BookingDetails({ booking, showtime, movie, cinema }) {
  if (!booking) return null
  const tickets = booking.tickets || []
  return (
    <div className="booking-detail-grid">
      <section className="panel booking-main">
        <div className="section-heading"><div><span className="eyebrow"><Ticket /> Mã đặt vé</span><h2>{booking.bookingCode}</h2></div><StatusBadge value={booking.status} /></div>
        <div className="detail-list">
          <div><span>Phim</span><strong>{displayValue(tickets[0]?.movieTitle || movie?.title)}</strong></div>
          <div><span>Rạp</span><strong>{displayValue(tickets[0]?.cinemaName || cinema?.name)}</strong></div>
          <div><span>Phòng</span><strong>{displayValue(tickets[0]?.hallName)}</strong></div>
          <div><span>Suất chiếu</span><strong>{showtime ? `${formatDate(showtime.showDate)} · ${formatTime(showtime.startTime)}` : displayValue(tickets[0]?.showDate && `${formatDate(tickets[0].showDate)} · ${formatTime(tickets[0].startTime)}`)}</strong></div>
          <div><span>Ghế</span><strong>{(booking.seats || []).map((seat) => seat.seatCode).join(', ') || tickets.map((ticket) => ticket.seatCode).join(', ') || 'Chưa cập nhật'}</strong></div>
          <div><span>Tổng tiền chính thức</span><strong className="price-text">{formatCurrency(booking.totalAmount)}</strong></div>
          <div><span>Thanh toán</span><strong>{booking.payment ? `${booking.payment.method || '—'} · ${booking.payment.status || '—'}` : 'Chưa cập nhật'}</strong></div>
          <div><span>Thời gian tạo</span><strong>{formatDateTime(booking.createdAt)}</strong></div>
        </div>
        <div className="form-actions ticket-actions"><Link className="button button-secondary" to="/account/bookings">Quay lại lịch sử</Link><button type="button" className="button button-primary" onClick={() => window.print()}><Printer /> In vé</button><Link className="button button-ghost" to="/"><Home /> Trang chủ</Link></div>
      </section>
      <aside className="ticket-stack">
        {tickets.length ? tickets.map((ticket) => (
          <article className="ticket-card" key={ticket.id || ticket.ticketCode}>
            <span className="ticket-notch top" /><span className="ticket-notch bottom" />
            <SafeImage src={ticket.moviePosterUrl} alt="Poster vé" className="ticket-poster" />
            <div><span className="eyebrow">Vé điện tử</span><h3>{displayValue(ticket.movieTitle || movie?.title)}</h3><p>Ghế <strong>{displayValue(ticket.seatCode)}</strong> · {displayValue(ticket.seatType)}</p><p>Mã vé: {displayValue(ticket.ticketCode)}</p><StatusBadge value={ticket.status} /></div>
            {ticket.ticketCode && <SafeImage src={notificationApi.qrUrl(ticket.ticketCode)} fallback="/poster-fallback.svg" alt={`QR vé ${ticket.ticketCode}`} className="qr-image" />}
          </article>
        )) : <div className="panel"><h3>Vé đang được cập nhật</h3><p className="muted">Backend chưa trả metadata vé cho booking này.</p></div>}
      </aside>
    </div>
  )
}
