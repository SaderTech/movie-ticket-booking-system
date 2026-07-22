import { Link } from 'react-router-dom'
import { formatCurrency, formatTime } from '../../utils/formatters'
import { EmptyState } from '../common/AsyncState'

export function ShowtimeGroups({ showtimes = [], cinemas = [], hallNames = {}, allowBooking = true }) {
  const visible = showtimes.filter((item) => item.status !== 'CANCELLED')
  const grouped = visible.reduce((acc, item) => {
    const key = item.cinemaId
    if (!acc[key]) acc[key] = []
    acc[key].push(item)
    return acc
  }, {})
  if (!visible.length) return <EmptyState title="Chưa có lịch chiếu" message="Không có suất chiếu khả dụng trong ngày đã chọn." />
  return (
    <div className="showtime-groups">
      {Object.entries(grouped).map(([cinemaId, items]) => {
        const cinema = cinemas.find((entry) => String(entry.id) === String(cinemaId))
        return (
          <section className="showtime-group" key={cinemaId}>
            <div><h3>{cinema?.name || `Rạp #${cinemaId}`}</h3><p>{cinema?.address || 'Địa chỉ đang cập nhật'}</p></div>
            <div className="showtime-buttons">
              {items.sort((a, b) => String(a.startTime).localeCompare(String(b.startTime))).map((showtime) => (
                allowBooking && showtime.status === 'AVAILABLE'
                  ? <Link to={`/booking/${showtime.id}/seats`} className="showtime-button" key={showtime.id}>
                      <strong>{formatTime(showtime.startTime)}</strong>
                      <span>{formatCurrency(showtime.price)}</span>
                      <small>{showtime.availableSeats ?? 0} ghế · {hallNames[showtime.roomId] || `Phòng ${showtime.roomId}`}</small>
                    </Link>
                  : <div className="showtime-button disabled" key={showtime.id}>
                      <strong>{formatTime(showtime.startTime)}</strong><span>{showtime.status === 'FULL' ? 'Hết chỗ' : 'Không khả dụng'}</span>
                    </div>
              ))}
            </div>
          </section>
        )
      })}
    </div>
  )
}
