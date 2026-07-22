import { Building2, ChevronRight, MapPin, Navigation, Phone } from 'lucide-react'
import { Link } from 'react-router-dom'
import { StatusBadge } from '../common/StatusBadge'

export function CinemaCard({ cinema, index = 0 }) {
  const hasCoordinates = cinema.latitude != null && cinema.longitude != null
  const mapsUrl = hasCoordinates ? `https://www.google.com/maps?q=${cinema.latitude},${cinema.longitude}` : null
  return (
    <article className={`cinema-card cinema-tone-${index % 3}`}>
      <div className="cinema-card-glow" aria-hidden="true" />
      <div className="cinema-icon"><Building2 /></div>
      <div className="cinema-card-content">
        <div className="card-title-row"><h2>{cinema.name}</h2><StatusBadge value={cinema.status} /></div>
        <p><MapPin /> {cinema.address}, {cinema.city}</p>
        <p><Phone /> {cinema.contactPhone || 'Chưa cập nhật'}</p>
        <div className="cinema-actions">
          <Link className="button button-primary button-sm" to={`/cinemas/${cinema.id}`}>Xem lịch chiếu <ChevronRight /></Link>
          {mapsUrl && <a className="icon-button" href={mapsUrl} target="_blank" rel="noreferrer" aria-label={`Mở bản đồ ${cinema.name}`}><Navigation /></a>}
        </div>
      </div>
    </article>
  )
}
