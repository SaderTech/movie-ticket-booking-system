import { AnimatePresence, motion } from 'framer-motion'
import { X } from 'lucide-react'
import { useEffect, useRef } from 'react'

function useOverlay(open, onClose) {
  const panelRef = useRef(null)
  const onCloseRef = useRef(onClose)
  useEffect(() => { onCloseRef.current = onClose }, [onClose])
  useEffect(() => {
    if (!open) return undefined
    const previous = document.activeElement
    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    const onKeyDown = (event) => {
      if (event.key === 'Escape') onCloseRef.current?.()
      if (event.key === 'Tab' && panelRef.current) {
        const focusable = panelRef.current.querySelectorAll('a[href], button:not(:disabled), input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])')
        if (!focusable.length) return
        const first = focusable[0]
        const last = focusable[focusable.length - 1]
        if (event.shiftKey && document.activeElement === first) { event.preventDefault(); last.focus() }
        if (!event.shiftKey && document.activeElement === last) { event.preventDefault(); first.focus() }
      }
    }
    document.addEventListener('keydown', onKeyDown)
    window.setTimeout(() => panelRef.current?.querySelector('button, a, input, select, textarea')?.focus(), 30)
    return () => {
      document.body.style.overflow = previousOverflow
      document.removeEventListener('keydown', onKeyDown)
      previous?.focus?.()
    }
  }, [open])
  return panelRef
}

export function Modal({ open, onClose, title, children, size = 'md', labelledBy }) {
  const panelRef = useOverlay(open, onClose)
  const titleId = labelledBy || 'dialog-title'
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="modal-backdrop" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onMouseDown={(event) => event.target === event.currentTarget && onClose?.()}>
          <motion.section ref={panelRef} className={`modal-card modal-${size}`} role="dialog" aria-modal="true" aria-labelledby={titleId} initial={{ opacity: 0, scale: 0.96, y: 14 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.98, y: 8 }} transition={{ duration: 0.22 }}>
            <button type="button" className="modal-close icon-button" onClick={onClose} aria-label="Đóng hộp thoại"><X /></button>
            {title && <h2 id={titleId}>{title}</h2>}
            {children}
          </motion.section>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

export function Drawer({ open, onClose, title = 'Trình đơn', children, side = 'right' }) {
  const panelRef = useOverlay(open, onClose)
  const direction = side === 'left' ? -1 : 1
  return (
    <AnimatePresence>
      {open && (
        <motion.div className="drawer-backdrop" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onMouseDown={(event) => event.target === event.currentTarget && onClose?.()}>
          <motion.aside ref={panelRef} className={`drawer-panel drawer-${side}`} role="dialog" aria-modal="true" aria-label={title} initial={{ x: `${direction * 100}%` }} animate={{ x: 0 }} exit={{ x: `${direction * 100}%` }} transition={{ type: 'tween', duration: 0.28, ease: [0.22, 1, 0.36, 1] }}>
            <div className="drawer-header"><strong>{title}</strong><button className="icon-button" onClick={onClose} aria-label="Đóng trình đơn"><X /></button></div>
            {children}
          </motion.aside>
        </motion.div>
      )}
    </AnimatePresence>
  )
}

export function Dropdown({ open, onClose, children, className = '' }) {
  const ref = useRef(null)
  useEffect(() => {
    if (!open) return undefined
    const close = (event) => { if (!ref.current?.contains(event.target)) onClose?.() }
    const key = (event) => { if (event.key === 'Escape') onClose?.() }
    document.addEventListener('pointerdown', close)
    document.addEventListener('keydown', key)
    return () => { document.removeEventListener('pointerdown', close); document.removeEventListener('keydown', key) }
  }, [open, onClose])
  return <div ref={ref} className={`dropdown-anchor ${className}`}>{children}</div>
}

export function ConfirmDialog({ open, onClose, onConfirm, title = 'Xác nhận thao tác', message, confirmLabel = 'Xác nhận', pending = false, danger = false }) {
  return (
    <Modal open={open} onClose={onClose} title={title}>
      <p className="modal-message">{message}</p>
      <div className="form-actions">
        <button type="button" className="button button-secondary" onClick={onClose}>Quay lại</button>
        <button type="button" className={`button ${danger ? 'button-danger solid' : 'button-primary'}`} disabled={pending} onClick={onConfirm}>{pending ? 'Đang xử lý…' : confirmLabel}</button>
      </div>
    </Modal>
  )
}
