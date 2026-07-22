const labels = {
  NOW_SHOWING: 'Đang chiếu', COMING_SOON: 'Sắp chiếu', ENDED: 'Đã kết thúc',
  ACTIVE: 'Hoạt động', INACTIVE: 'Ngừng hoạt động', MAINTENANCE: 'Bảo trì',
  AVAILABLE: 'Còn chỗ', FULL: 'Hết chỗ', CANCELLED: 'Đã hủy',
  HOLDING: 'Đang giữ ghế', PENDING_PAYMENT: 'Chờ thanh toán', CONFIRMED: 'Đã xác nhận', EXPIRED: 'Đã hết hạn', FAILED: 'Thất bại',
  PAID: 'Đã thanh toán', PENDING: 'Đang chờ', SENT: 'Đã gửi', RETRYING: 'Đang thử lại',
  SCHEDULED: 'Đã lên lịch', IN_PROGRESS: 'Đang thực hiện', COMPLETED: 'Hoàn tất',
  BROKEN: 'Hỏng', DISABLED: 'Vô hiệu hóa', USED: 'Đã sử dụng',
}

export function StatusBadge({ value }) {
  if (!value) return <span className="badge neutral">Chưa cập nhật</span>
  const tone = ['ACTIVE', 'AVAILABLE', 'CONFIRMED', 'PAID', 'SENT', 'COMPLETED', 'NOW_SHOWING'].includes(value)
    ? 'success'
    : ['CANCELLED', 'FAILED', 'BROKEN', 'DISABLED', 'ENDED'].includes(value) ? 'danger' : 'warning'
  return <span className={`badge ${tone}`}>{labels[value] || String(value).replaceAll('_', ' ')}</span>
}
