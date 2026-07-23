import { Armchair } from 'lucide-react'
import { Tooltip } from '../common/DesignSystem'

export function SeatMap({ seats = [], selected = [], onToggle, readonly = false }) {
  const sorted = [...seats].sort((a, b) => String(a.rowName).localeCompare(String(b.rowName), 'vi', { numeric: true }) || Number(a.seatNumber) - Number(b.seatNumber))
  const rows = sorted.reduce((acc, seat) => {
    if (!acc[seat.rowName]) acc[seat.rowName] = []
    acc[seat.rowName].push(seat)
    return acc
  }, {})
  return <div className="seat-map" aria-label="Sơ đồ ghế">
    <div className="screen"><span>MÀN HÌNH</span><i /></div>
    <div className="seat-map-scroll"><div className="seat-rows">{Object.entries(rows).map(([row, rowSeats]) => <div className="seat-row" key={row}><span className="row-label">{row}</span><div className="row-seats">{rowSeats.map((seat) => {
      const code = `${seat.rowName}${seat.seatNumber}`
      const active = seat.status === 'ACTIVE' && !seat.bookingStatus
      const picked = selected.includes(code)
      const unavailableState = seat.bookingStatus === 'BOOKED' ? 'sold' : seat.bookingStatus === 'HELD' ? 'held' : seat.status === 'BROKEN' ? 'broken' : seat.status === 'DISABLED' ? 'disabled' : 'unavailable'
      const availabilityLabel = seat.bookingStatus === 'BOOKED' ? 'Đã bán' : seat.bookingStatus === 'HELD' ? 'Đang được giữ' : seat.status === 'BROKEN' ? 'Ghế hỏng' : seat.status === 'DISABLED' ? 'Ghế bị vô hiệu hóa' : 'Không khả dụng'
      return <Tooltip key={seat.id} content={`${code} · ${seat.seatTypeName || 'Tiêu chuẩn'} · ${active ? 'Có thể chọn' : availabilityLabel}`}><button type="button" className={`seat ${picked ? 'selected' : ''} ${active ? '' : `unavailable ${unavailableState}`} type-${String(seat.seatTypeCode || '').toLowerCase()}`} disabled={!active || readonly} onClick={() => onToggle?.(seat)} aria-pressed={picked} aria-label={`Ghế ${code}, ${seat.seatTypeName || 'tiêu chuẩn'}, ${active ? 'có thể chọn' : availabilityLabel}`}><Armchair aria-hidden="true" /><span>{seat.seatNumber}</span></button></Tooltip>
    })}</div><span className="row-label">{row}</span></div>)}</div></div>
    <div className="seat-legend"><span><i className="seat sample" /> Ghế thường</span><span><i className="seat sample type-vip" /> Ghế VIP</span><span><i className="seat sample selected" /> Đang chọn</span><span><i className="seat sample broken" /> Ghế hỏng</span><span><i className="seat sample sold" /> Đã bán</span><span><i className="seat sample held" /> Đang giữ</span><span><i className="seat sample disabled" /> Vô hiệu hóa</span></div>
  </div>
}
