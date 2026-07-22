import { ChevronLeft, ChevronRight } from 'lucide-react'

export function Pagination({ page = 0, totalPages = 1, onPageChange }) {
  if (totalPages <= 1) return null
  return (
    <nav className="pagination" aria-label="Phân trang">
      <button className="icon-button" disabled={page <= 0} onClick={() => onPageChange(page - 1)} aria-label="Trang trước"><ChevronLeft /></button>
      <span>Trang {page + 1} / {totalPages}</span>
      <button className="icon-button" disabled={page >= totalPages - 1} onClick={() => onPageChange(page + 1)} aria-label="Trang sau"><ChevronRight /></button>
    </nav>
  )
}
