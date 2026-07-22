import { CalendarDays, Clock, Info, Ticket } from 'lucide-react'
import { Link } from 'react-router-dom'
import { formatDate } from '../../utils/formatters'
import { SafeImage } from '../common/SafeImage'
import { StatusBadge } from '../common/StatusBadge'

export function MovieCard({ movie }) {
  const genres = (movie.genres || []).map((item) => item.name).filter(Boolean).slice(0, 2).join(' · ')
  return (
    <article className="movie-card">
      <Link to={`/movies/${movie.id}`} className="poster-wrap" aria-label={`Xem chi tiết ${movie.title}`}>
        <SafeImage src={movie.posterUrl} alt={`Poster ${movie.title}`} className="movie-poster" loading="lazy" />
        <span className="poster-shade" />
        <span className={`age-badge age-${movie.ageRating || 'P'}`}>{movie.ageRating || 'P'}</span>
        <span className="movie-status"><StatusBadge value={movie.status} /></span>
        <span className="poster-cta"><Ticket /> Mua vé</span>
      </Link>
      <div className="movie-card-body">
        <h3><Link to={`/movies/${movie.id}`}>{movie.title}</Link></h3>
        <p className="movie-genres">{genres || 'Thể loại đang cập nhật'}</p>
        <div className="movie-meta"><span><Clock /> {movie.durationMinutes ? `${movie.durationMinutes} phút` : '—'}</span>{movie.status === 'COMING_SOON' && <span><CalendarDays /> {formatDate(movie.releaseDate)}</span>}</div>
        <div className="card-actions">
          <Link className="button button-secondary button-sm" to={`/movies/${movie.id}`}><Info size={16} /> Chi tiết</Link>
          <Link className="button button-primary button-sm" to={`/movies/${movie.id}#showtimes`}><Ticket size={16} /> Mua vé</Link>
        </div>
      </div>
    </article>
  )
}
