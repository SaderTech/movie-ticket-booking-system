const statusMessages = {
  400: 'Dữ liệu gửi lên chưa hợp lệ.',
  401: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  403: 'Bạn không có quyền thực hiện thao tác này.',
  404: 'Không tìm thấy dữ liệu yêu cầu.',
  409: 'Dữ liệu đang xung đột. Vui lòng tải lại và thử lại.',
  429: 'Bạn thao tác quá nhanh, vui lòng thử lại sau.',
  500: 'Hệ thống đang gặp sự cố. Vui lòng thử lại sau.',
}

function firstDetail(details) {
  if (Array.isArray(details)) return details.filter(Boolean).join(', ')
  if (details && typeof details === 'object') return Object.values(details).flat().filter(Boolean).join(', ')
  return details
}

export function getApiError(error, fallback = 'Không thể hoàn tất yêu cầu.') {
  const status = error?.response?.status
  const data = error?.response?.data
  if (typeof data === 'string' && data.trim()) return data
  const message = data?.message || data?.error || firstDetail(data?.details) || data?.errorCode
  return message || statusMessages[status] || (error?.request ? 'Không thể kết nối API Gateway.' : fallback)
}

export function getApiErrorCode(error) {
  return error?.response?.data?.errorCode || null
}
