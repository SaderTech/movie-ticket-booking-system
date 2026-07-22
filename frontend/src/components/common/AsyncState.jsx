import { AlertTriangle, Clapperboard, Inbox, RefreshCw } from 'lucide-react'

export function SkeletonGrid({ count = 4, compact = false }) {
  return (
    <div className={compact ? 'skeleton-list' : 'movie-grid'} aria-label="Đang tải dữ liệu" aria-busy="true">
      {Array.from({ length: count }, (_, index) => (
        <div className={`skeleton-card ${compact ? 'compact' : ''}`} key={index}>
          <div className="skeleton skeleton-image" />
          <div className="skeleton skeleton-line wide" />
          <div className="skeleton skeleton-line" />
        </div>
      ))}
    </div>
  )
}

export function EmptyState({ title = 'Chưa có dữ liệu', message = 'Dữ liệu sẽ xuất hiện tại đây khi có cập nhật.', action }) {
  return (
    <div className="state-card">
      <span className="state-icon"><Clapperboard size={32} aria-hidden="true" /><Inbox size={18} aria-hidden="true" /></span>
      <h3>{title}</h3>
      <p>{message}</p>
      {action}
    </div>
  )
}

export function ErrorState({ message = 'Không thể tải dữ liệu.', onRetry }) {
  return (
    <div className="state-card error-state" role="alert">
      <span className="state-icon danger"><AlertTriangle size={32} aria-hidden="true" /></span>
      <h3>Đã xảy ra lỗi</h3>
      <p>{message}</p>
      {onRetry && <button className="button button-secondary" onClick={onRetry}><RefreshCw size={17} /> Thử lại</button>}
    </div>
  )
}
