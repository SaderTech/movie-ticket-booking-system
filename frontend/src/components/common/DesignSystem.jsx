import { LoaderCircle, Search, X } from 'lucide-react'

export function LoadingSpinner({ label = 'Đang xử lý', size = 18 }) {
  return <span className="loading-spinner" role="status"><LoaderCircle className="spin" size={size} aria-hidden="true" /><span className="sr-only">{label}</span></span>
}

export function Button({ children, variant = 'primary', size = '', loading = false, className = '', type = 'button', ...props }) {
  return <button type={type} className={`button button-${variant} ${size ? `button-${size}` : ''} ${className}`.trim()} disabled={loading || props.disabled} {...props}>{loading && <LoadingSpinner />}{children}</button>
}

export function IconButton({ label, children, className = '', ...props }) {
  return <button type="button" className={`icon-button ${className}`.trim()} aria-label={label} {...props}>{children}</button>
}

export function SectionTitle({ eyebrow, title, description, action, as = 'h2' }) {
  const Heading = as
  return <div className="section-heading"><div>{eyebrow && <span className="eyebrow">{eyebrow}</span>}<Heading>{title}</Heading>{description && <p>{description}</p>}</div>{action}</div>
}

export function PageHero({ eyebrow, title, description, children, compact = false }) {
  return <section className={`page-hero ${compact ? 'compact' : ''}`}><div className="container page-hero-inner"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></div>{children}</div></section>
}

export function Badge({ children, tone = 'neutral', className = '' }) {
  return <span className={`badge ${tone} ${className}`.trim()}>{children}</span>
}

export function SearchInput({ value, onChange, onClear, placeholder = 'Tìm kiếm…', label = 'Tìm kiếm' }) {
  return <div className="search-field"><Search aria-hidden="true" /><label className="sr-only">{label}</label><input value={value} onChange={onChange} placeholder={placeholder} />{value && <button type="button" onClick={onClear} aria-label="Xóa từ khóa"><X /></button>}</div>
}

export function Tabs({ items, value, onChange, label = 'Bộ lọc' }) {
  return <div className="tabs" role="tablist" aria-label={label}>{items.map((item) => { const key = Array.isArray(item) ? item[0] : item.value; const text = Array.isArray(item) ? item[1] : item.label; return <button type="button" role="tab" aria-selected={value === key} className={value === key ? 'active' : ''} key={key || text} onClick={() => onChange(key)}>{text}</button> })}</div>
}

export function DateSelector({ dates, value, onChange }) {
  return <div className="date-strip" role="list" aria-label="Chọn ngày chiếu">{dates.map((date) => { const key = date.value; return <button type="button" role="listitem" aria-pressed={value === key} className={value === key ? 'active' : ''} onClick={() => onChange(key)} key={key}><span>{date.weekday}</span><strong>{date.day}</strong><small>{date.month}</small></button> })}</div>
}

export function FormField({ label, error, hint, children, className = '' }) {
  return <label className={className}>{label}{children}{error && <small className="field-error">{error}</small>}{!error && hint && <small>{hint}</small>}</label>
}

export function SelectField({ label, error, children, className = '', ...props }) {
  return <FormField label={label} error={error} className={className}><select {...props}>{children}</select></FormField>
}

export function Tooltip({ content, children }) {
  return <span className="tooltip" data-tooltip={content}>{children}</span>
}

export function Skeleton({ className = '' }) {
  return <span className={`skeleton ${className}`.trim()} aria-hidden="true" />
}

export function SkeletonTable({ rows = 5, columns = 4 }) {
  return <div className="skeleton-table" aria-label="Đang tải bảng dữ liệu" aria-busy="true">{Array.from({ length: rows }, (_, row) => <div key={row}>{Array.from({ length: columns }, (_, col) => <Skeleton className="skeleton-line" key={col} />)}</div>)}</div>
}

export function LoadingOverlay({ visible, label = 'Đang xử lý…' }) {
  if (!visible) return null
  return <div className="loading-overlay" role="status"><LoadingSpinner size={26} /><span>{label}</span></div>
}
