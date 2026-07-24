import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BellRing, Copy, Eye, Mail, Plus, QrCode, RefreshCcw, RotateCcw, Search, Send, Sparkles, Users } from 'lucide-react'
import { useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import { notificationApi } from '../../api/notificationApi'
import { userApi } from '../../api/userApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Modal } from '../../components/common/Overlay'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { displayValue, formatDateTime } from '../../utils/formatters'

const types = [
  'SYSTEM_ALERT',
  'BOOKING_CONFIRMATION',
  'TICKET_BOOKED',
  'SHOWTIME_REMINDER',
  'BOOKING_CANCELLED',
  'SEAT_HOLD_CREATED',
  'SEAT_HOLD_EXPIRED',
  'PAYMENT_SUCCESS',
  'PAYMENT_FAILED',
  'PAYMENT_REFUND_REQUIRED',
]

const statusOptions = ['', 'SENT', 'PENDING', 'RETRYING', 'FAILED']
const emptyNotification = { recipientEmail: '', subject: '', message: '', channel: 'EMAIL', type: 'SYSTEM_ALERT' }
const emptyTemplate = { code: '', subject: '', body: '' }

const adminSendTypes = ['SYSTEM_ALERT', 'SHOWTIME_REMINDER']

const quickMessages = [
  {
    label: 'Ngày lễ giảm giá',
    payload: {
      type: 'SYSTEM_ALERT',
      subject: 'Ưu đãi ngày lễ từ Movie Ticket',
      message: 'Xin chào,\n\nMovie Ticket đang có chương trình ưu đãi ngày lễ dành cho khách hàng. Hãy theo dõi các suất chiếu nổi bật và đặt vé sớm để nhận giá tốt.\n\nChúc bạn có trải nghiệm xem phim vui vẻ.',
    },
  },
  {
    label: 'Phim đáng chú ý',
    payload: {
      type: 'SYSTEM_ALERT',
      subject: 'Các phim đáng chú ý trong tuần',
      message: 'Xin chào,\n\nMovie Ticket gợi ý một số phim đáng chú ý trong tuần này. Bạn có thể truy cập trang phim để xem lịch chiếu và chọn ghế phù hợp.\n\nCảm ơn bạn đã sử dụng Movie Ticket.',
    },
  },
  {
    label: 'Bảo trì hệ thống',
    payload: {
      type: 'SYSTEM_ALERT',
      subject: 'Thông báo bảo trì hệ thống',
      message: 'Xin chào,\n\nHệ thống Movie Ticket sẽ bảo trì trong thời gian ngắn. Một số chức năng như đặt vé hoặc thanh toán có thể bị gián đoạn.\n\nCảm ơn bạn đã thông cảm.',
    },
  },
  {
    label: 'Đóng/sửa rạp',
    payload: {
      type: 'SYSTEM_ALERT',
      subject: 'Thông báo điều chỉnh hoạt động rạp',
      message: 'Xin chào,\n\nMột số rạp/phòng chiếu có thể tạm đóng hoặc sửa chữa theo kế hoạch vận hành. Vui lòng kiểm tra lịch chiếu mới nhất trước khi đặt vé.\n\nCảm ơn bạn đã đồng hành cùng Movie Ticket.',
    },
  },
  {
    label: 'Nhắc lịch',
    payload: {
      type: 'SHOWTIME_REMINDER',
      subject: 'Nhắc lịch xem phim',
      message: 'Xin chào,\n\nSuất chiếu của bạn sắp bắt đầu. Vui lòng đến rạp sớm để check-in và ổn định chỗ ngồi.\n\nChúc bạn xem phim vui vẻ.',
    },
  },
]

const templatePresets = [
  {
    code: 'BOOKING_CONFIRMATION',
    subject: 'Xác nhận đặt vé thành công - {{bookingCode}}',
    body: 'Xin chào {{customerName}},\n\nBạn đã đặt vé thành công.\nMã đặt vé: {{bookingCode}}\nPhim: {{movieTitle}}\nSuất chiếu: {{showtimeLabel}}\nGhế: {{seatCodes}}\nTổng tiền: {{totalAmount}}\n\nEmail vé điện tử sẽ được gửi kèm mã QR sau khi ticket code được phát hành.',
  },
  {
    code: 'TICKET_BOOKED',
    subject: 'Vé xem phim đã sẵn sàng - {{bookingCode}}',
    body: 'Xin chào {{customerName}},\n\nVé của bạn đã được phát hành thành công.\nMã đặt vé: {{bookingCode}}\nPhim: {{movieTitle}}\nSuất chiếu: {{showtimeLabel}}\nGhế: {{seatCodes}}\nMã vé/QR: {{ticketCodes}}\n\nMã QR của từng ticket code đã được đính kèm trong email.',
  },
  {
    code: 'PAYMENT_SUCCESS',
    subject: 'Thanh toán thành công - {{paymentCode}}',
    body: 'Xin chào {{customerName}},\n\nThanh toán {{paymentCode}} cho mã đặt vé {{bookingCode}} đã thành công.\nSố tiền: {{amount}}.',
  },
]

function Heading({ title, description, action }) {
  return <div className="admin-page-title"><div><span className="eyebrow"><BellRing /> Notification service</span><h1>{title}</h1><p>{description}</p></div>{action}</div>
}

function copyText(value, label = 'Đã copy') {
  if (!value) return
  navigator.clipboard?.writeText(value)
  toast.success(label)
}

function splitRecipients(value) {
  return String(value || '')
    .split(/[\n,;]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

export function AdminNotificationsPage() {
  const [form, setForm] = useState({ ...emptyNotification })
  const [recipientMode, setRecipientMode] = useState('ALL_CUSTOMERS')
  const [lastResult, setLastResult] = useState(null)
  const [ticketCode, setTicketCode] = useState('TCK_SAMPLE')
  const ping = useQuery({ queryKey: ['notification', 'ping'], queryFn: notificationApi.ping, retry: false })

  async function fetchAllCustomerEmails() {
    const emails = new Set()
    let page = 0
    let totalPages = 1
    do {
      const data = await userApi.list({ page, size: 100 })
      ;(data.content || []).forEach((user) => {
        if (user?.email) emails.add(user.email)
      })
      totalPages = data.totalPages || 1
      page += 1
    } while (page < totalPages)
    return Array.from(emails)
  }

  const send = useMutation({
    mutationFn: async (payload) => {
      const recipients = recipientMode === 'ALL_CUSTOMERS'
        ? await fetchAllCustomerEmails()
        : splitRecipients(payload.recipientEmail)

      if (!recipients.length) {
        throw new Error(recipientMode === 'ALL_CUSTOMERS' ? 'Không tìm thấy email khách hàng nào từ User Service' : 'Vui lòng nhập ít nhất một email người nhận')
      }

      const cleanPayload = { ...payload, recipientEmail: undefined }
      const results = await Promise.all(recipients.map((email) => notificationApi.send({ ...cleanPayload, recipientEmail: email })))
      const failed = results.filter((item) => item.status !== 'SENT')
      return {
        ...results[0],
        id: results.map((item) => item.id).join(', '),
        recipientEmail: recipientMode === 'ALL_CUSTOMERS' ? `Toàn bộ khách hàng (${recipients.length})` : `${recipients.length} người nhận`,
        status: failed.length ? 'RETRYING' : 'SENT',
        errorMessage: failed.length ? `${failed.length}/${results.length} notification chưa SENT` : null,
        bulkResults: results,
      }
    },
    onSuccess: (data) => {
      setLastResult(data)
      toast.success(data.status === 'SENT' ? 'Thông báo đã gửi' : `Backend trả trạng thái ${data.status}`)
    },
    onError: (error) => toast.error(getApiError(error)),
  })

  const applyQuick = (preset) => setForm((current) => ({ ...current, ...preset.payload }))

  return <div>
    <Heading title="Tạo notification" description="Dùng cho admin gửi thông báo hệ thống, ưu đãi, phim đáng chú ý, bảo trì hoặc nhắc lịch; mặc định có thể gửi toàn bộ khách hàng." />
    <div className="service-status"><span className={ping.isSuccess ? 'online' : 'offline'} /> Notification service: {ping.isLoading ? 'đang kiểm tra' : ping.isSuccess ? 'kết nối được' : 'không phản hồi'}</div>

    <section className="admin-two-column notification-compose-grid">
      <form className="panel admin-form notification-compose-card" onSubmit={(event) => { event.preventDefault(); send.mutate(form) }}>
        <div className="section-heading"><div><span className="eyebrow"><Send /> Soạn thông báo</span><h2>Gửi notification cho khách hàng</h2></div></div>
        <div className="quick-preset-row">{quickMessages.map((item) => <button key={item.label} type="button" className="button button-ghost compact" onClick={() => applyQuick(item)}><Sparkles /> {item.label}</button>)}</div>

        <div className="recipient-mode-box">
          <button type="button" className={`button compact ${recipientMode === 'ALL_CUSTOMERS' ? 'button-primary' : 'button-secondary'}`} onClick={() => setRecipientMode('ALL_CUSTOMERS')}><Users /> Toàn bộ khách hàng</button>
          <button type="button" className={`button compact ${recipientMode === 'CUSTOM' ? 'button-primary' : 'button-secondary'}`} onClick={() => setRecipientMode('CUSTOM')}><Mail /> Chọn vài email</button>
        </div>

        {recipientMode === 'CUSTOM'
          ? <label>Email người nhận<input required type="text" placeholder="customer@gmail.com hoặc nhiều email cách nhau bằng dấu phẩy/xuống dòng" value={form.recipientEmail} onChange={(event) => setForm({ ...form, recipientEmail: event.target.value })} /></label>
          : <p className="info-note">Chế độ này sẽ lấy danh sách email từ User Service rồi gửi lần lượt cho từng khách hàng. Có thể chuyển sang “Chọn vài email” để gửi cho một nhóm nhỏ.</p>}

        <div className="form-grid two"><label>Loại thông báo<select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>{adminSendTypes.map((item) => <option key={item} value={item}>{item}</option>)}</select></label><label>Kênh<select value={form.channel} onChange={(event) => setForm({ ...form, channel: event.target.value })}><option value="EMAIL">EMAIL</option></select></label></div>
        <label>Tiêu đề<input required value={form.subject} onChange={(event) => setForm({ ...form, subject: event.target.value })} /></label>
        <label>Nội dung<textarea required rows="9" value={form.message} onChange={(event) => setForm({ ...form, message: event.target.value })} /></label>
        <p className="info-note">Admin notification chỉ dùng cho thông báo chung. Gửi vé/QR cho khách sẽ đi qua Kafka event <strong>booking.ticket-booked</strong> để backend lấy đúng <strong>ticketCode</strong>.</p>
        <div className="form-actions movie-form-actions"><button type="button" className="button button-secondary" onClick={() => { setForm({ ...emptyNotification }); setRecipientMode('ALL_CUSTOMERS') }}>Xóa form</button><button className="button button-primary" disabled={send.isPending}><Send /> {send.isPending ? 'Đang gửi...' : recipientMode === 'ALL_CUSTOMERS' ? 'Gửi toàn bộ khách hàng' : 'Gửi nhóm đã chọn'}</button></div>
      </form>

      <div className="panel notification-result-panel">
        <div className="section-heading"><div><span className="eyebrow"><QrCode /> QR & kết quả</span><h2>Kiểm tra nhanh</h2></div>{lastResult && <StatusBadge value={lastResult.status} />}</div>
        <label>Ticket code để test QR<input value={ticketCode} onChange={(event) => setTicketCode(event.target.value)} /></label>
        <div className="qr-preview-box">
          {ticketCode ? <img src={notificationApi.qrUrl(ticketCode)} alt={`QR ${ticketCode}`} /> : <span>Nhập ticket code để xem QR</span>}
        </div>
        <div className="form-actions"><a className="button button-secondary" href={notificationApi.qrUrl(ticketCode || 'TCK_SAMPLE')} target="_blank" rel="noreferrer"><QrCode /> Mở QR ticket</a></div>
        {lastResult ? <div className="notification-result"><dl><div><dt>ID</dt><dd><button className="link-button" onClick={() => copyText(lastResult.id, 'Đã copy ID')}>{lastResult.id}</button></dd></div><div><dt>Người nhận</dt><dd>{lastResult.recipientEmail}</dd></div><div><dt>Loại</dt><dd>{lastResult.type}</dd></div><div><dt>Thời gian</dt><dd>{formatDateTime(lastResult.createdAt)}</dd></div><div><dt>Lỗi backend</dt><dd>{displayValue(lastResult.errorMessage, 'Không có')}</dd></div></dl></div> : <EmptyState title="Chưa gửi notification" message="Chọn preset, chọn toàn bộ khách hàng hoặc nhập vài email để gửi thử." />}
      </div>
    </section>
  </div>
}

export function AdminNotificationTemplatesPage() {
  const queryClient = useQueryClient()
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState({ ...emptyTemplate })
  const query = useQuery({ queryKey: ['admin', 'notification-templates'], queryFn: notificationApi.templates })
  const closeForm = () => { setFormOpen(false); setForm({ ...emptyTemplate }) }
  const create = useMutation({ mutationFn: notificationApi.createTemplate, onSuccess: () => { toast.success('Đã tạo mẫu thông báo'); queryClient.invalidateQueries({ queryKey: ['admin', 'notification-templates'] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })

  return <div>
    <Heading title="Mẫu email" description="Lưu các mẫu nội dung email để admin dễ quản lý và dùng lại." action={<button className="button button-primary" onClick={() => setFormOpen(true)}><Plus /> Tạo mẫu</button>} />
    <section className="panel template-preset-panel"><div className="section-heading"><div><span className="eyebrow"><Sparkles /> Preset</span><h2>Mẫu đề xuất</h2></div></div><div className="template-preset-grid">{templatePresets.map((item) => <button key={item.code} className="template-preset-card" onClick={() => { setForm(item); setFormOpen(true) }}><strong>{item.code}</strong><span>{item.subject}</span></button>)}</div></section>
    <Modal open={formOpen} onClose={create.isPending ? undefined : closeForm} title="Tạo mẫu thông báo">
      <form className="admin-form admin-modal-form" onSubmit={(event) => { event.preventDefault(); create.mutate(form) }}><label>Mã mẫu<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} /></label><label>Tiêu đề<input required value={form.subject} onChange={(event) => setForm({ ...form, subject: event.target.value })} /></label><label>Nội dung mẫu<textarea required rows="10" value={form.body} onChange={(event) => setForm({ ...form, body: event.target.value })} /></label><div className="form-actions movie-form-actions"><button type="button" className="button button-secondary" disabled={create.isPending} onClick={closeForm}>Hủy</button><button className="button button-primary" disabled={create.isPending}><Plus /> {create.isPending ? 'Đang tạo...' : 'Tạo mẫu'}</button></div></form>
    </Modal>
    <section className="template-list">{query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} /> : !(query.data || []).length ? <EmptyState title="Chưa có mẫu" /> : query.data.map((item) => <article className="panel template-card" key={item.id}><Mail /><div><span className="eyebrow">{item.code}</span><h2>{item.subject}</h2><p>{item.body}</p></div></article>)}</section>
  </div>
}

export function AdminNotificationLogsPage() {
  const queryClient = useQueryClient()
  const [filters, setFilters] = useState({ status: '', type: '', email: '', page: 0, size: 10 })
  const [selectedId, setSelectedId] = useState(null)
  const params = useMemo(() => Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '' && value !== null && value !== undefined)), [filters])
  const query = useQuery({ queryKey: ['admin', 'notification-logs', params], queryFn: () => notificationApi.searchLogs(params), refetchInterval: 30000 })
  const detail = useQuery({ queryKey: ['admin', 'notification-log', selectedId], queryFn: () => notificationApi.getLog(selectedId), enabled: Boolean(selectedId) })
  const resend = useMutation({ mutationFn: notificationApi.resend, onSuccess: () => { toast.success('Đã gửi lại notification'); queryClient.invalidateQueries({ queryKey: ['admin', 'notification-logs'] }); if (selectedId) queryClient.invalidateQueries({ queryKey: ['admin', 'notification-log', selectedId] }) }, onError: (error) => toast.error(getApiError(error)) })
  const rows = query.data?.content || []
  const summary = rows.reduce((acc, item) => ({ ...acc, [item.status]: (acc[item.status] || 0) + 1 }), {})

  return <div>
    <Heading title="Nhật ký gửi notification" description="Lọc log theo trạng thái, loại thông báo và email; tự làm mới mỗi 30 giây." action={<button className="button button-secondary" onClick={() => query.refetch()}><RefreshCcw /> Làm mới</button>} />
    <section className="panel notification-filter-panel"><div className="form-grid four"><label>Trạng thái<select value={filters.status} onChange={(event) => setFilters({ ...filters, status: event.target.value, page: 0 })}>{statusOptions.map((item) => <option key={item || 'ALL'} value={item}>{item || 'Tất cả'}</option>)}</select></label><label>Loại<select value={filters.type} onChange={(event) => setFilters({ ...filters, type: event.target.value, page: 0 })}><option value="">Tất cả</option>{types.map((item) => <option key={item}>{item}</option>)}</select></label><label>Email<input value={filters.email} onChange={(event) => setFilters({ ...filters, email: event.target.value, page: 0 })} placeholder="customer@gmail.com" /></label><label>Số dòng<select value={filters.size} onChange={(event) => setFilters({ ...filters, size: Number(event.target.value), page: 0 })}><option value={10}>10</option><option value={20}>20</option><option value={50}>50</option></select></label></div><div className="notification-summary-row"><span><Search /> Tổng: {query.data?.totalElements ?? rows.length}</span><span>SENT: {summary.SENT || 0}</span><span>FAILED: {summary.FAILED || 0}</span><span>RETRYING: {summary.RETRYING || 0}</span><span>PENDING: {summary.PENDING || 0}</span></div></section>
    {query.isLoading ? <SkeletonGrid count={6} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !rows.length ? <EmptyState title="Chưa có nhật ký phù hợp" /> : <section className="panel table-panel"><div className="table-scroll"><table><thead><tr><th>Người nhận</th><th>Thông báo</th><th>Kênh / Loại</th><th>Trạng thái</th><th>Thử lại</th><th>Thời gian</th><th></th></tr></thead><tbody>{rows.map((item) => <tr key={item.id}><td>{item.recipientEmail}</td><td><strong>{item.subject}</strong><small className="cell-clamp">{item.message}</small>{item.errorMessage && <small className="field-error">{item.errorMessage}</small>}</td><td>{item.channel}<small>{item.type}</small></td><td><StatusBadge value={item.status} /></td><td>{item.retryCount}/{item.maxRetries}</td><td>{formatDateTime(item.sentAt || item.createdAt)}</td><td><div className="row-actions"><button className="icon-button" title="Xem chi tiết" onClick={() => setSelectedId(item.id)}><Eye /></button><button className="icon-button" title="Copy ID" onClick={() => copyText(item.id, 'Đã copy ID')}><Copy /></button>{item.status !== 'SENT' && <button className="icon-button" title="Gửi lại" onClick={() => resend.mutate(item.id)}><RotateCcw /></button>}</div></td></tr>)}</tbody></table></div><div className="table-footer"><button className="button button-secondary" disabled={(filters.page || 0) <= 0} onClick={() => setFilters({ ...filters, page: filters.page - 1 })}>Trang trước</button><span>Trang {(query.data?.page ?? filters.page) + 1}/{query.data?.totalPages || 1}</span><button className="button button-secondary" disabled={(query.data?.page ?? filters.page) + 1 >= (query.data?.totalPages || 1)} onClick={() => setFilters({ ...filters, page: filters.page + 1 })}>Trang sau</button></div></section>}
    <Modal open={Boolean(selectedId)} onClose={() => setSelectedId(null)} title="Chi tiết notification">
      {detail.isLoading ? <SkeletonGrid count={2} compact /> : detail.isError ? <ErrorState message={getApiError(detail.error)} /> : detail.data && <div className="notification-detail"><div className="section-heading"><div><span className="eyebrow">{detail.data.type}</span><h2>{detail.data.subject}</h2></div><StatusBadge value={detail.data.status} /></div><dl><div><dt>ID</dt><dd>{detail.data.id}</dd></div><div><dt>Người nhận</dt><dd>{detail.data.recipientEmail}</dd></div><div><dt>Source</dt><dd>{displayValue(detail.data.sourceTopic)} / {displayValue(detail.data.sourceEventId)}</dd></div><div><dt>Retry</dt><dd>{detail.data.retryCount}/{detail.data.maxRetries}</dd></div><div><dt>Lỗi</dt><dd>{displayValue(detail.data.errorMessage, 'Không có')}</dd></div></dl><pre>{detail.data.message}</pre>{detail.data.status !== 'SENT' && <div className="form-actions"><button className="button button-primary" onClick={() => resend.mutate(detail.data.id)}><RotateCcw /> Gửi lại</button></div>}</div>}
    </Modal>
  </div>
}
