import { EmptyState, SkeletonGrid } from '../common/AsyncState'
import { StaggerGroup, StaggerItem } from '../common/Motion'
import { MovieCard } from './MovieCard'

export function MovieGrid({ movies = [], isLoading, emptyTitle }) {
  if (isLoading) return <SkeletonGrid />
  if (!movies.length) return <EmptyState title={emptyTitle || 'Chưa có phim'} message="Backend hiện chưa trả về phim phù hợp." />
  return <StaggerGroup className="movie-grid">{movies.map((movie) => <StaggerItem key={movie.id}><MovieCard movie={movie} /></StaggerItem>)}</StaggerGroup>
}
