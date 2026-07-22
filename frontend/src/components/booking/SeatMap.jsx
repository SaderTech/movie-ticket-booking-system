import { Armchair } from 'lucide-react'
import { Tooltip } from '../common/DesignSystem'

export function SeatMap({ seats = [], selected = [], onToggle, readonly = false }) {
  const sorted = [...seats].sort((a, b) => String(a.rowName).localeCompare(String(b.rowName), 'vi', { numeric: true }) || Number(a.seatNumber) - Number(b.seatNumber))
  const rows = sorted.reduce((acc, seat) => {
    if (!acc[seat.rowName]) acc[seat.rowName] = []
    acc[seat.rowName].push(seat)
    return acc
  }, {})
  return (
    <div className="seat-map" aria-label="Sơ đồ ghế">
      <div className="screen"><span>MÀN HÌNH</span><i /></div>
      <div className="seat-map-scroll"><div className="seat-rows">
        {Object.entries(rows).map(([row, rowSeats]) => (
          <div className="seat-row" key={row}>
            <span className="row-label">{row}</span>
            <div className="row-seats">{rowSeats.map((seat) => {
              const code = `${seat.rowName}${seat.seatNumber}`
              const active = seat.status === 'ACTIVE'
              const picked = selected.includes(code)
              return <Tooltip key={seat.id} content={`${code} · ${seat.seatTypeName || 'Tiêu chuẩn'} · ${active ? 'Có thể chọn' : seat.status || 'Không khả dụng'}`}><button type="button" className={`seat ${picked ? 'selected' : ''} ${active ? '' : 'unavailable'} type-${String(seat.seatTypeCode || '').toLowerCase()}`} disabled={!active || readonly} onClick={() => onToggle?.(seat)} aria-pressed={picked} aria-label={`Ghế ${code}, ${seat.seatTypeName || 'tiêu chuẩn'}, ${active ? 'có thể chọn' : 'không khả dụng'}`}><Armchair aria-hidden="true" /><span>{seat.seatNumber}</span></button></Tooltip>
            })}</div>
            <span className="row-label">{row}</span>
          </div>
        ))}
      </div></div>
      <div className="seat-legend">
        <span><i className="seat sample" /> Ghế thường</span>
        <span><i className="seat sample type-vip" /> Ghế VIP</span>
        <span><i className="seat sample selected" /> Đang chọn</span>
        <span><i className="seat sample unavailable" /> Không khả dụng</span>
      </div>
      <p className="muted small">Trạng thái hiển thị là trạng thái tĩnh của phòng. Hệ thống sẽ kiểm tra xung đột thực tế khi giữ ghế.</p>
    </div>
  )
}
