import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Edit3, Plus, Save, Search, Trash2, Wrench } from 'lucide-react'
import { useEffect, useState } from 'react'
import toast from 'react-hot-toast'
import { bookingApi } from '../../api/bookingApi'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { showtimeApi } from '../../api/showtimeApi'
import { userApi } from '../../api/userApi'
import { SeatMap } from '../../components/booking/SeatMap'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Modal } from '../../components/common/Overlay'
import { Pagination } from '../../components/common/Pagination'
import { SafeImage } from '../../components/common/SafeImage'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { displayValue, formatCurrency, formatDate, formatDateTime, formatTime } from '../../utils/formatters'

function AdminHeading({ eyebrow = 'Vận hành rạp', title, description, action }) {
  return <div className="admin-page-title"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></div>{action}</div>
}

function CinemaHallFilters({ cinemas = [], cinemaId, setCinemaId, halls = [], hallId, setHallId, requireHall = true }) {
  return <div className="panel inline-filters"><label>Rạp<select value={cinemaId} onChange={(event) => { setCinemaId(event.target.value); setHallId?.('') }}><option value="">Chọn rạp</option>{cinemas.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>{requireHall && <label>Phòng<select value={hallId} onChange={(event) => setHallId(event.target.value)} disabled={!cinemaId}><option value="">Chọn phòng</option>{halls.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select></label>}</div>
}

function ModalActions({ pending, onCancel, submitLabel }) {
  return <div className="form-actions movie-form-actions"><button type="button" className="button button-secondary" disabled={pending} onClick={onCancel}>Hủy</button><button className="button button-primary" disabled={pending}><Save /> {pending ? 'Đang lưu...' : submitLabel}</button></div>
}

const emptyHall = { name: '', capacity: '', hallType: 'STANDARD', status: 'ACTIVE' }

export function AdminHallsPage() {
  const queryClient = useQueryClient()
  const [cinemaId, setCinemaId] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ ...emptyHall })
  const cinemas = useQuery({ queryKey: ['admin', 'cinemas'], queryFn: () => cinemaApi.list() })
  useEffect(() => { if (!cinemaId && cinemas.data?.length) setCinemaId(String(cinemas.data[0].id)) }, [cinemas.data, cinemaId])
  const halls = useQuery({ queryKey: ['admin', 'halls', cinemaId], queryFn: () => cinemaApi.halls(cinemaId), enabled: Boolean(cinemaId) })

  const closeForm = () => { setFormOpen(false); setEditing(null); setForm({ ...emptyHall }) }
  const openCreate = () => { setEditing(null); setForm({ ...emptyHall }); setFormOpen(true) }
  const openEdit = (hall) => { setEditing(hall); setForm({ name: hall.name, capacity: hall.capacity, hallType: hall.hallType, status: hall.status }); setFormOpen(true) }
  const save = useMutation({ mutationFn: (payload) => editing ? cinemaApi.updateHall(editing.id, payload) : cinemaApi.createHall({ cinemaId: Number(cinemaId), ...payload }), onSuccess: () => { toast.success('Đã lưu phòng chiếu'); queryClient.invalidateQueries({ queryKey: ['admin', 'halls', cinemaId] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })
  const submit = (event) => { event.preventDefault(); save.mutate({ ...form, capacity: Number(form.capacity) }) }

  return <div>
    <AdminHeading title="Phòng chiếu" description="Chọn rạp để xem danh sách phòng. Backend không có API xóa phòng." action={<button className="button button-primary" disabled={!cinemaId} onClick={openCreate}><Plus /> Thêm phòng</button>} />
    <CinemaHallFilters cinemas={cinemas.data || []} cinemaId={cinemaId} setCinemaId={(value) => { setCinemaId(value); closeForm() }} requireHall={false} />
    <Modal open={formOpen} onClose={save.isPending ? undefined : closeForm} title={editing ? `Sửa phòng ${editing.name}` : 'Thêm phòng chiếu'}>
      <form className="admin-form admin-modal-form" onSubmit={submit}><label>Tên phòng<input required value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} /></label><label>Sức chứa<input required min="1" type="number" value={form.capacity} onChange={(event) => setForm({ ...form, capacity: event.target.value })} /></label><label>Loại phòng<select value={form.hallType} onChange={(event) => setForm({ ...form, hallType: event.target.value })}>{['STANDARD', 'VIP', 'IMAX', 'FOUR_DX'].map((item) => <option key={item}>{item}</option>)}</select></label><label>Trạng thái<select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>{['ACTIVE', 'INACTIVE', 'MAINTENANCE'].map((item) => <option key={item}>{item}</option>)}</select></label><ModalActions pending={save.isPending} onCancel={closeForm} submitLabel={editing ? 'Lưu thay đổi' : 'Tạo phòng'} /></form>
    </Modal>
    <section className="panel table-panel"><div className="section-heading"><h2>Danh sách phòng</h2><span className="badge neutral">{halls.data?.length || 0} phòng</span></div>{halls.isLoading ? <SkeletonGrid count={4} compact /> : halls.isError ? <ErrorState message={getApiError(halls.error)} /> : !(halls.data || []).length ? <EmptyState title={cinemaId ? 'Rạp chưa có phòng' : 'Hãy chọn rạp'} /> : <div className="table-scroll"><table><thead><tr><th>Phòng</th><th>Sức chứa</th><th>Loại</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{halls.data.map((hall) => <tr key={hall.id}><td><strong>{hall.name}</strong></td><td>{hall.capacity}</td><td>{hall.hallType}</td><td><StatusBadge value={hall.status} /></td><td><button className="button button-secondary button-sm" onClick={() => openEdit(hall)}><Edit3 /> Sửa</button></td></tr>)}</tbody></table></div>}</section>
  </div>
}

const emptySeat = { seatTypeId: '', rowName: '', seatNumber: '', status: 'ACTIVE' }

export function AdminSeatsPage() {
  const queryClient = useQueryClient()
  const [cinemaId, setCinemaId] = useState('')
  const [hallId, setHallId] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ ...emptySeat })
  const cinemas = useQuery({ queryKey: ['admin', 'cinemas'], queryFn: () => cinemaApi.list() })
  const halls = useQuery({ queryKey: ['admin', 'halls', cinemaId], queryFn: () => cinemaApi.halls(cinemaId), enabled: Boolean(cinemaId) })
  const types = useQuery({ queryKey: ['admin', 'seat-types'], queryFn: cinemaApi.seatTypes })
  const seats = useQuery({ queryKey: ['admin', 'seats', hallId], queryFn: () => cinemaApi.seats(hallId), enabled: Boolean(hallId) })

  const closeForm = () => { setFormOpen(false); setEditing(null); setForm({ ...emptySeat }) }
  const openCreate = () => { setEditing(null); setForm({ ...emptySeat }); setFormOpen(true) }
  const openEdit = (seat) => { setEditing(seat); setForm({ seatTypeId: String(seat.seatTypeId), rowName: seat.rowName, seatNumber: seat.seatNumber, status: seat.status }); setFormOpen(true) }
  const save = useMutation({ mutationFn: (payload) => editing ? cinemaApi.updateSeat(editing.id, payload) : cinemaApi.createSeat({ hallId: Number(hallId), ...payload }), onSuccess: () => { toast.success('Đã lưu ghế'); queryClient.invalidateQueries({ queryKey: ['admin', 'seats', hallId] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })
  const submit = (event) => { event.preventDefault(); save.mutate({ seatTypeId: Number(form.seatTypeId), rowName: form.rowName.trim().toUpperCase(), seatNumber: Number(form.seatNumber), status: form.status }) }

  return <div>
    <AdminHeading title="Ghế" description="Chọn rạp và phòng để xem sơ đồ ghế. Backend không có endpoint tạo hàng loạt hoặc xóa ghế." action={<button className="button button-primary" disabled={!hallId} onClick={openCreate}><Plus /> Thêm ghế</button>} />
    <CinemaHallFilters cinemas={cinemas.data || []} cinemaId={cinemaId} setCinemaId={(value) => { setCinemaId(value); closeForm() }} halls={halls.data || []} hallId={hallId} setHallId={(value) => { setHallId(value); closeForm() }} />
    <Modal open={formOpen} onClose={save.isPending ? undefined : closeForm} title={editing ? `Sửa ghế ${editing.rowName}${editing.seatNumber}` : 'Thêm ghế'}>
      <form className="admin-form admin-modal-form" onSubmit={submit}><label>Loại ghế<select required value={form.seatTypeId} onChange={(event) => setForm({ ...form, seatTypeId: event.target.value })}><option value="">Chọn loại ghế</option>{(types.data || []).map((item) => <option value={item.id} key={item.id}>{item.code} — {item.name}</option>)}</select></label><label>Hàng<input required maxLength="10" value={form.rowName} onChange={(event) => setForm({ ...form, rowName: event.target.value })} /></label><label>Số ghế<input required min="1" type="number" value={form.seatNumber} onChange={(event) => setForm({ ...form, seatNumber: event.target.value })} /></label><label>Trạng thái<select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>{['ACTIVE', 'BROKEN', 'DISABLED'].map((item) => <option key={item}>{item}</option>)}</select></label><ModalActions pending={save.isPending} onCancel={closeForm} submitLabel={editing ? 'Lưu thay đổi' : 'Tạo ghế'} /></form>
    </Modal>
    <section className="panel"><div className="section-heading"><h2>Danh sách ghế</h2><span className="badge neutral">{seats.data?.length || 0} ghế</span></div>{seats.isLoading ? <SkeletonGrid count={4} compact /> : seats.isError ? <ErrorState message={getApiError(seats.error)} /> : !(seats.data || []).length ? <EmptyState title={hallId ? 'Phòng chưa có ghế' : 'Hãy chọn phòng'} /> : <><SeatMap seats={seats.data} readonly /><div className="table-scroll compact-table"><table><thead><tr><th>Ghế</th><th>Loại</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{seats.data.map((seat) => <tr key={seat.id}><td>{seat.rowName}{seat.seatNumber}</td><td>{seat.seatTypeName}</td><td><StatusBadge value={seat.status} /></td><td><button className="button button-secondary button-sm" onClick={() => openEdit(seat)}><Edit3 /> Sửa</button></td></tr>)}</tbody></table></div></>}</section>
  </div>
}

const emptyMaintenance = { startTime: '', endTime: '', reason: '' }

export function AdminMaintenancesPage() {
  const queryClient = useQueryClient()
  const [cinemaId, setCinemaId] = useState('')
  const [hallId, setHallId] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState({ ...emptyMaintenance })
  const cinemas = useQuery({ queryKey: ['admin', 'cinemas'], queryFn: () => cinemaApi.list() })
  const halls = useQuery({ queryKey: ['admin', 'halls', cinemaId], queryFn: () => cinemaApi.halls(cinemaId), enabled: Boolean(cinemaId) })
  const items = useQuery({ queryKey: ['admin', 'maintenances', hallId], queryFn: () => cinemaApi.maintenances(hallId), enabled: Boolean(hallId) })
  const closeForm = () => { setFormOpen(false); setForm({ ...emptyMaintenance }) }
  const create = useMutation({ mutationFn: () => cinemaApi.createMaintenance({ hallId: Number(hallId), startTime: form.startTime, endTime: form.endTime, reason: form.reason }), onSuccess: () => { toast.success('Đã tạo lịch bảo trì'); queryClient.invalidateQueries({ queryKey: ['admin', 'maintenances', hallId] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })
  const status = useMutation({ mutationFn: ({ id, value }) => cinemaApi.updateMaintenanceStatus(id, value), onSuccess: () => { toast.success('Đã cập nhật trạng thái'); queryClient.invalidateQueries({ queryKey: ['admin', 'maintenances', hallId] }) }, onError: (error) => toast.error(getApiError(error)) })
  const submit = (event) => { event.preventDefault(); if (new Date(form.endTime) <= new Date(form.startTime)) return toast.error('Thời gian kết thúc phải sau thời gian bắt đầu'); create.mutate() }

  return <div>
    <AdminHeading title="Bảo trì phòng" description="Chọn rạp và phòng để xem danh sách lịch bảo trì." action={<button className="button button-primary" disabled={!hallId} onClick={() => setFormOpen(true)}><Wrench /> Tạo lịch bảo trì</button>} />
    <CinemaHallFilters cinemas={cinemas.data || []} cinemaId={cinemaId} setCinemaId={(value) => { setCinemaId(value); closeForm() }} halls={halls.data || []} hallId={hallId} setHallId={(value) => { setHallId(value); closeForm() }} />
    <Modal open={formOpen} onClose={create.isPending ? undefined : closeForm} title="Tạo lịch bảo trì">
      <form className="admin-form admin-modal-form" onSubmit={submit}><label>Bắt đầu<input required type="datetime-local" value={form.startTime} onChange={(event) => setForm({ ...form, startTime: event.target.value })} /></label><label>Kết thúc<input required type="datetime-local" value={form.endTime} onChange={(event) => setForm({ ...form, endTime: event.target.value })} /></label><label>Lý do<textarea rows="4" value={form.reason} onChange={(event) => setForm({ ...form, reason: event.target.value })} /></label><ModalActions pending={create.isPending} onCancel={closeForm} submitLabel="Tạo lịch" /></form>
    </Modal>
    <section className="panel table-panel"><div className="section-heading"><h2>Danh sách bảo trì</h2><span className="badge neutral">{items.data?.length || 0} lịch</span></div>{items.isLoading ? <SkeletonGrid count={4} compact /> : items.isError ? <ErrorState message={getApiError(items.error)} /> : !(items.data || []).length ? <EmptyState title={hallId ? 'Chưa có lịch bảo trì' : 'Hãy chọn phòng'} /> : <div className="maintenance-list">{items.data.map((item) => <article key={item.id}><div><span>{formatDateTime(item.startTime)} → {formatDateTime(item.endTime)}</span><strong>{item.reason || 'Không có ghi chú'}</strong></div><select value={item.status} onChange={(event) => status.mutate({ id: item.id, value: event.target.value })}>{['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].map((value) => <option key={value}>{value}</option>)}</select></article>)}</div>}</section>
  </div>
}

const emptyShowtime = { movieId: '', cinemaId: '', roomId: '', showDate: '', startTime: '', endTime: '', price: '', availableSeats: '', status: 'AVAILABLE' }

export function AdminShowtimesPage() {
  const queryClient = useQueryClient()
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ ...emptyShowtime })
  const items = useQuery({ queryKey: ['admin', 'showtimes'], queryFn: showtimeApi.list })
  const movies = useQuery({ queryKey: ['admin', 'movies'], queryFn: () => movieApi.list() })
  const cinemas = useQuery({ queryKey: ['admin', 'cinemas'], queryFn: () => cinemaApi.list() })
  const halls = useQuery({ queryKey: ['admin', 'halls', form.cinemaId], queryFn: () => cinemaApi.halls(form.cinemaId), enabled: Boolean(form.cinemaId) })
  const closeForm = () => { setFormOpen(false); setEditing(null); setForm({ ...emptyShowtime }) }
  const openCreate = () => { setEditing(null); setForm({ ...emptyShowtime }); setFormOpen(true) }
  const openEdit = (item) => { setEditing(item); setForm({ ...item, movieId: String(item.movieId), cinemaId: String(item.cinemaId), roomId: String(item.roomId), startTime: formatTime(item.startTime), endTime: formatTime(item.endTime) }); setFormOpen(true) }
  const save = useMutation({ mutationFn: (payload) => editing ? showtimeApi.update(editing.id, payload) : showtimeApi.create(payload), onSuccess: () => { toast.success('Đã lưu suất chiếu'); queryClient.invalidateQueries({ queryKey: ['admin', 'showtimes'] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })
  const remove = useMutation({ mutationFn: showtimeApi.remove, onSuccess: () => { toast.success('Đã xóa suất chiếu'); queryClient.invalidateQueries({ queryKey: ['admin', 'showtimes'] }) }, onError: (error) => toast.error(getApiError(error)) })
  const payload = () => ({ movieId: Number(form.movieId), cinemaId: Number(form.cinemaId), roomId: Number(form.roomId), showDate: form.showDate, startTime: form.startTime.length === 5 ? `${form.startTime}:00` : form.startTime, endTime: form.endTime.length === 5 ? `${form.endTime}:00` : form.endTime, price: Number(form.price), availableSeats: Number(form.availableSeats), status: form.status })
  const submit = (event) => { event.preventDefault(); if (form.endTime <= form.startTime) return toast.error('Giờ kết thúc phải sau giờ bắt đầu'); save.mutate(payload()) }

  return <div>
    <AdminHeading eyebrow="Lịch chiếu" title="Suất chiếu" description="Danh sách suất chiếu hiện có. Backend hỗ trợ tạo, sửa và xóa." action={<button className="button button-primary" onClick={openCreate}><Plus /> Thêm suất chiếu</button>} />
    <Modal open={formOpen} onClose={save.isPending ? undefined : closeForm} title={editing ? 'Sửa suất chiếu' : 'Thêm suất chiếu'} size="xl">
      <form className="admin-showtime-form" onSubmit={submit}><div className="form-grid four"><label>Phim<select required value={form.movieId} onChange={(event) => setForm({ ...form, movieId: event.target.value })}><option value="">Chọn phim</option>{(movies.data || []).map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}</select></label><label>Rạp<select required value={form.cinemaId} onChange={(event) => setForm({ ...form, cinemaId: event.target.value, roomId: '' })}><option value="">Chọn rạp</option>{(cinemas.data || []).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Phòng<select required disabled={!form.cinemaId} value={form.roomId} onChange={(event) => setForm({ ...form, roomId: event.target.value })}><option value="">Chọn phòng</option>{(halls.data || []).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select></label><label>Ngày chiếu<input required type="date" value={form.showDate} onChange={(event) => setForm({ ...form, showDate: event.target.value })} /></label><label>Giờ bắt đầu<input required type="time" step="1" value={form.startTime} onChange={(event) => setForm({ ...form, startTime: event.target.value })} /></label><label>Giờ kết thúc<input required type="time" step="1" value={form.endTime} onChange={(event) => setForm({ ...form, endTime: event.target.value })} /></label><label>Giá vé<input required min="1" type="number" value={form.price} onChange={(event) => setForm({ ...form, price: event.target.value })} /></label><label>Ghế còn trống<input required min="0" type="number" value={form.availableSeats} onChange={(event) => setForm({ ...form, availableSeats: event.target.value })} /></label><label>Trạng thái<select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>{['AVAILABLE', 'FULL', 'CANCELLED'].map((item) => <option key={item}>{item}</option>)}</select></label></div><ModalActions pending={save.isPending} onCancel={closeForm} submitLabel={editing ? 'Lưu thay đổi' : 'Tạo suất chiếu'} /></form>
    </Modal>
    <section className="panel table-panel"><div className="section-heading"><h2>Danh sách suất chiếu</h2><span className="badge neutral">{items.data?.length || 0} suất</span></div>{items.isLoading ? <SkeletonGrid count={5} compact /> : items.isError ? <ErrorState message={getApiError(items.error)} /> : !(items.data || []).length ? <EmptyState title="Chưa có suất chiếu" /> : <div className="table-scroll"><table><thead><tr><th>Phim</th><th>Rạp / Phòng</th><th>Thời gian</th><th>Giá</th><th>Ghế</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>{items.data.map((item) => <tr key={item.id}><td>{movies.data?.find((movie) => movie.id === item.movieId)?.title || `#${item.movieId}`}</td><td>{cinemas.data?.find((cinema) => cinema.id === item.cinemaId)?.name || `Rạp #${item.cinemaId}`}<small>Phòng #{item.roomId}</small></td><td>{formatDate(item.showDate)}<small>{formatTime(item.startTime)} – {formatTime(item.endTime)}</small></td><td>{formatCurrency(item.price)}</td><td>{item.availableSeats}</td><td><StatusBadge value={item.status} /></td><td><div className="row-actions"><button className="button button-secondary button-sm" onClick={() => openEdit(item)}><Edit3 /> Sửa</button><button className="button button-danger button-sm" onClick={() => window.confirm('Xóa suất chiếu này?') && remove.mutate(item.id)}><Trash2 /> Xóa</button></div></td></tr>)}</tbody></table></div>}</section>
  </div>
}

export function AdminUsersPage() {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ fullName: '', phone: '', avatar: '' })
  useEffect(() => { const timer = setTimeout(() => { setSearch(keyword.trim()); setPage(0) }, 500); return () => clearTimeout(timer) }, [keyword])
  const users = useQuery({ queryKey: ['admin', 'users', search, page], queryFn: () => userApi.list({ keyword: search || undefined, page, size: 10 }) })
  const closeForm = () => { setEditing(null); setForm({ fullName: '', phone: '', avatar: '' }) }
  const openEdit = (user) => { setEditing(user); setForm({ fullName: user.fullName || '', phone: user.phone || '', avatar: user.avatar || '' }) }
  const update = useMutation({ mutationFn: () => userApi.update(editing.id, { ...form, phone: form.phone || null }), onSuccess: () => { toast.success('Đã cập nhật người dùng'); queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })
  const remove = useMutation({ mutationFn: userApi.remove, onSuccess: () => { toast.success('Đã xóa người dùng'); queryClient.invalidateQueries({ queryKey: ['admin', 'users'] }) }, onError: (error) => toast.error(getApiError(error)) })

  return <div>
    <AdminHeading eyebrow="Tài khoản" title="Người dùng" description="Danh sách người dùng được phân trang tại server. Backend không trả role trong response này." />
    <div className="filter-bar admin-filter"><div className="search-field"><Search /><input value={keyword} onChange={(event) => setKeyword(event.target.value)} placeholder="Tìm username, email, họ tên..." /></div></div>
    <Modal open={Boolean(editing)} onClose={update.isPending ? undefined : closeForm} title={editing ? `Sửa ${editing.username}` : 'Sửa người dùng'}>
      <form className="admin-form admin-modal-form" onSubmit={(event) => { event.preventDefault(); update.mutate() }}>{editing && <p className="form-note">{editing.email}</p>}<label>Họ tên<input value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} /></label><label>Điện thoại<input value={form.phone} onChange={(event) => setForm({ ...form, phone: event.target.value })} pattern="(0|\+84)[0-9]{9}" /></label><label>Avatar URL<input type="url" value={form.avatar} onChange={(event) => setForm({ ...form, avatar: event.target.value })} /></label>{form.avatar && <div className="image-preview"><SafeImage src={form.avatar} alt="Xem trước avatar" /></div>}<ModalActions pending={update.isPending} onCancel={closeForm} submitLabel="Lưu thay đổi" /></form>
    </Modal>
    <section className="panel table-panel">{users.isLoading ? <SkeletonGrid count={5} compact /> : users.isError ? <ErrorState message={getApiError(users.error)} /> : !(users.data?.content || []).length ? <EmptyState title="Không có người dùng" /> : <><div className="table-scroll"><table><thead><tr><th>Người dùng</th><th>Email</th><th>Họ tên</th><th>Điện thoại</th><th>Thao tác</th></tr></thead><tbody>{users.data.content.map((user) => <tr key={user.id}><td><div className="user-cell"><SafeImage src={user.avatar} className="table-avatar" alt={user.username} /><strong>{user.username}</strong></div></td><td>{user.email}</td><td>{displayValue(user.fullName)}</td><td>{displayValue(user.phone)}</td><td><div className="row-actions"><button className="button button-secondary button-sm" onClick={() => openEdit(user)}><Edit3 /> Sửa</button><button className="button button-danger button-sm" onClick={() => window.confirm(`Xóa người dùng ${user.username}?`) && remove.mutate(user.id)}><Trash2 /> Xóa</button></div></td></tr>)}</tbody></table></div><Pagination page={users.data.page ?? page} totalPages={users.data.totalPages || 1} onPageChange={setPage} /></>}</section>
  </div>
}

export function AdminBookingSettingsPage() {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(null)
  const [value, setValue] = useState('')
  const query = useQuery({ queryKey: ['admin', 'booking-settings'], queryFn: bookingApi.settings })
  const closeForm = () => { setEditing(null); setValue('') }
  const openEdit = (item) => { setEditing(item); setValue(item.settingValue ?? '') }
  const update = useMutation({ mutationFn: () => bookingApi.updateSetting(editing.settingKey, value), onSuccess: () => { toast.success('Đã cập nhật cấu hình'); queryClient.invalidateQueries({ queryKey: ['admin', 'booking-settings'] }); closeForm() }, onError: (error) => toast.error(getApiError(error)) })

  return <div>
    <AdminHeading eyebrow="Booking service" title="Cấu hình đặt vé" description="Danh sách cấu hình được tải trực tiếp từ API. Bấm Sửa để thay đổi từng giá trị." />
    <Modal open={Boolean(editing)} onClose={update.isPending ? undefined : closeForm} title={editing ? `Sửa ${editing.settingKey}` : 'Sửa cấu hình'}>
      <form className="admin-form admin-modal-form" onSubmit={(event) => { event.preventDefault(); update.mutate() }}>{editing?.description && <p className="form-note">{editing.description}</p>}<label>Giá trị<input required value={value} onChange={(event) => setValue(event.target.value)} /></label><ModalActions pending={update.isPending} onCancel={closeForm} submitLabel="Cập nhật" /></form>
    </Modal>
    {query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} /> : !(query.data || []).length ? <EmptyState title="Backend chưa trả cấu hình" /> : <div className="settings-grid">{query.data.map((item) => <article className="panel setting-card" key={item.settingKey}><span className="eyebrow">{item.settingKey}</span><p>{item.description || 'Không có mô tả'}</p><div className="setting-value"><span>Giá trị hiện tại</span><strong>{displayValue(item.settingValue)}</strong></div><button className="button button-secondary button-sm" onClick={() => openEdit(item)}><Edit3 /> Sửa cấu hình</button></article>)}</div>}
  </div>
}
