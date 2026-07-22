import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { BellRing, Mail, Plus, Send } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'
import { notificationApi } from '../../api/notificationApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Modal } from '../../components/common/Overlay'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { displayValue, formatDateTime } from '../../utils/formatters'

const types = ['BOOKING_CONFIRMATION', 'BOOKING_CANCELLED', 'SEAT_HOLD_CREATED', 'SEAT_HOLD_EXPIRED', 'PAYMENT_SUCCESS', 'PAYMENT_FAILED', 'SHOWTIME_REMINDER', 'TICKET_CANCELLED', 'SYSTEM_ALERT']
const emptyNotification = { recipientEmail: '', subject: '', message: '', channel: 'EMAIL', type: 'SYSTEM_ALERT' }
const emptyTemplate = { code: '', subject: '', body: '' }

function Heading({ title, description, action }) {
  return <div className="admin-page-title"><div><span className="eyebrow"><BellRing /> Notification service</span><h1>{title}</h1><p>{description}</p></div>{action}</div>
}

export function AdminNotificationsPage() {
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState({ ...emptyNotification })
  const [lastResult, setLastResult] = useState(null)
  const ping = useQuery({ queryKey: ['notification', 'ping'], queryFn: notificationApi.ping, retry: false })
  const closeForm = () => { setFormOpen(false); setForm({ ...emptyNotification }) }
  const send = useMutation({ mutationFn: notificationApi.send, onSuccess: (data) => { setLastResult(data); toast.success(data.status === 'SENT' ? 'Thông báo đã gửi' : `Backend trả trạng thái ${data.status}`); closeForm() }, onError: (error) => toast.error(getApiError(error)) })

  return <div>
    <Heading title="Gửi thông báo" description="Theo dõi kết quả gửi gần nhất; mở biểu mẫu khi cần soạn thông báo mới." action={<button className="button button-primary" onClick={() => setFormOpen(true)}><Send /> Soạn thông báo</button>} />
    <div className="service-status"><span className={ping.isSuccess ? 'online' : 'offline'} /> Notification service: {ping.isLoading ? 'đang kiểm tra' : ping.isSuccess ? 'kết nối được' : 'không phản hồi'}</div>
    <Modal open={formOpen} onClose={send.isPending ? undefined : closeForm} title="Soạn thông báo">
      <form className="admin-form admin-modal-form" onSubmit={(event) => { event.preventDefault(); send.mutate(form) }}><label>Email người nhận<input required type="email" value={form.recipientEmail} onChange={(event) => setForm({ ...form, recipientEmail: event.target.value })} /></label><label>Tiêu đề<input required value={form.subject} onChange={(event) => setForm({ ...form, subject: event.target.value })} /></label><label>Nội dung<textarea required rows="7" value={form.message} onChange={(event) => setForm({ ...form, message: event.target.value })} /></label><label>Kênh<select value={form.channel} onChange={(event) => setForm({ ...form, channel: event.target.value })}><option value="EMAIL">EMAIL — đang hỗ trợ</option><option value="SMS">SMS — backend sẽ trả trạng thái thực tế</option></select></label><label>Loại thông báo<select value={form.type} onChange={(event) => setForm({ ...form, type: event.target.value })}>{types.map((item) => <option key={item}>{item}</option>)}</select></label><p className="info-note">Mã nguồn service hiện chỉ thực hiện gửi EMAIL; SMS có thể được ghi nhận với trạng thái thất bại.</p><div className="form-actions movie-form-actions"><button type="button" className="button button-secondary" disabled={send.isPending} onClick={closeForm}>Hủy</button><button className="button button-primary" disabled={send.isPending}><Send /> {send.isPending ? 'Đang gửi...' : 'Gửi thông báo'}</button></div></form>
    </Modal>
    <section className="panel notification-result-panel"><div className="section-heading"><h2>Kết quả gần nhất</h2>{lastResult && <StatusBadge value={lastResult.status} />}</div>{lastResult ? <div className="notification-result"><dl><div><dt>Người nhận</dt><dd>{lastResult.recipientEmail}</dd></div><div><dt>Kênh</dt><dd>{lastResult.channel}</dd></div><div><dt>Loại</dt><dd>{lastResult.type}</dd></div><div><dt>Thời gian</dt><dd>{formatDateTime(lastResult.createdAt)}</dd></div><div><dt>Lỗi backend</dt><dd>{displayValue(lastResult.errorMessage, 'Không có')}</dd></div></dl></div> : <EmptyState title="Chưa gửi thông báo" message="Bấm “Soạn thông báo” để bắt đầu." />}</section>
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
    <Heading title="Mẫu thông báo" description="Danh sách mẫu hiện có. Backend chỉ hỗ trợ tạo mới, không có chức năng sửa hoặc xóa." action={<button className="button button-primary" onClick={() => setFormOpen(true)}><Plus /> Tạo mẫu</button>} />
    <Modal open={formOpen} onClose={create.isPending ? undefined : closeForm} title="Tạo mẫu thông báo">
      <form className="admin-form admin-modal-form" onSubmit={(event) => { event.preventDefault(); create.mutate(form) }}><label>Mã mẫu<input required value={form.code} onChange={(event) => setForm({ ...form, code: event.target.value })} /></label><label>Tiêu đề<input required value={form.subject} onChange={(event) => setForm({ ...form, subject: event.target.value })} /></label><label>Nội dung mẫu<textarea required rows="8" value={form.body} onChange={(event) => setForm({ ...form, body: event.target.value })} /></label><div className="form-actions movie-form-actions"><button type="button" className="button button-secondary" disabled={create.isPending} onClick={closeForm}>Hủy</button><button className="button button-primary" disabled={create.isPending}><Plus /> {create.isPending ? 'Đang tạo...' : 'Tạo mẫu'}</button></div></form>
    </Modal>
    <section className="template-list">{query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} /> : !(query.data || []).length ? <EmptyState title="Chưa có mẫu" /> : query.data.map((item) => <article className="panel template-card" key={item.id}><Mail /><div><span className="eyebrow">{item.code}</span><h2>{item.subject}</h2><p>{item.body}</p></div></article>)}</section>
  </div>
}

export function AdminNotificationLogsPage() {
  const query = useQuery({ queryKey: ['admin', 'notification-logs'], queryFn: notificationApi.logs, refetchInterval: 30000 })
  return <div><Heading title="Nhật ký thông báo" description="Danh sách trạng thái gửi do API trả về; tự làm mới mỗi 30 giây." />{query.isLoading ? <SkeletonGrid count={6} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !(query.data || []).length ? <EmptyState title="Chưa có nhật ký" /> : <section className="panel table-panel"><div className="table-scroll"><table><thead><tr><th>Người nhận</th><th>Thông báo</th><th>Kênh / Loại</th><th>Trạng thái</th><th>Thử lại</th><th>Thời gian</th></tr></thead><tbody>{query.data.map((item) => <tr key={item.id}><td>{item.recipientEmail}</td><td><strong>{item.subject}</strong><small className="cell-clamp">{item.message}</small>{item.errorMessage && <small className="field-error">{item.errorMessage}</small>}</td><td>{item.channel}<small>{item.type}</small></td><td><StatusBadge value={item.status} /></td><td>{item.retryCount}/{item.maxRetries}</td><td>{formatDateTime(item.sentAt || item.createdAt)}</td></tr>)}</tbody></table></div></section>}</div>
}
