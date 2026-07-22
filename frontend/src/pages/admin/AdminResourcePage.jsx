import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Edit3, Image, Plus, RotateCcw, Save } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import toast from 'react-hot-toast'
import { cinemaApi } from '../../api/cinemaApi'
import { movieApi } from '../../api/movieApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { SafeImage } from '../../components/common/SafeImage'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { displayValue, formatDate, toNumber } from '../../utils/formatters'

const configs = {
  genres: { title: 'Thể loại', singular: 'thể loại', api: movieApi.genres, fields: [{ name: 'name', label: 'Tên thể loại', required: true }, { name: 'description', label: 'Mô tả', type: 'textarea' }], columns: [['name', 'Tên'], ['description', 'Mô tả']] },
  actors: { title: 'Diễn viên', singular: 'diễn viên', api: movieApi.actors, fields: [{ name: 'name', label: 'Tên diễn viên', required: true }, { name: 'avatarUrl', label: 'URL ảnh đại diện', type: 'url' }, { name: 'birthDate', label: 'Ngày sinh', type: 'date' }, { name: 'biography', label: 'Tiểu sử', type: 'textarea' }], columns: [['avatarUrl', 'Ảnh'], ['name', 'Tên'], ['birthDate', 'Ngày sinh'], ['biography', 'Tiểu sử']] },
  directors: { title: 'Đạo diễn', singular: 'đạo diễn', api: movieApi.directors, fields: [{ name: 'name', label: 'Tên đạo diễn', required: true }, { name: 'birthDate', label: 'Ngày sinh', type: 'date' }, { name: 'biography', label: 'Tiểu sử', type: 'textarea' }], columns: [['name', 'Tên'], ['birthDate', 'Ngày sinh'], ['biography', 'Tiểu sử']] },
  cinemas: { title: 'Rạp chiếu phim', singular: 'rạp', api: { list: cinemaApi.list, create: cinemaApi.create, update: cinemaApi.update }, fields: [{ name: 'name', label: 'Tên rạp', required: true }, { name: 'city', label: 'Thành phố', required: true }, { name: 'address', label: 'Địa chỉ', required: true }, { name: 'contactPhone', label: 'Điện thoại' }, { name: 'latitude', label: 'Vĩ độ', type: 'number', step: 'any' }, { name: 'longitude', label: 'Kinh độ', type: 'number', step: 'any' }, { name: 'status', label: 'Trạng thái', type: 'select', options: ['ACTIVE', 'INACTIVE'], required: true }], numeric: ['latitude', 'longitude'], defaults: { status: 'ACTIVE' }, columns: [['name', 'Tên rạp'], ['city', 'Thành phố'], ['address', 'Địa chỉ'], ['contactPhone', 'Điện thoại'], ['status', 'Trạng thái']] },
  'seat-types': { title: 'Loại ghế', singular: 'loại ghế', api: { list: cinemaApi.seatTypes, create: cinemaApi.createSeatType, update: cinemaApi.updateSeatType }, fields: [{ name: 'code', label: 'Mã loại ghế', required: true }, { name: 'name', label: 'Tên loại ghế', required: true }, { name: 'description', label: 'Mô tả', type: 'textarea' }], columns: [['code', 'Mã'], ['name', 'Tên'], ['description', 'Mô tả']] },
}

function initialValues(config) { return Object.fromEntries(config.fields.map((field) => [field.name, config.defaults?.[field.name] || ''])) }

export function AdminResourcePage({ resource }) {
  const config = configs[resource]
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(() => initialValues(config))
  useEffect(() => { setEditing(null); setForm(initialValues(config)) }, [resource])
  const query = useQuery({ queryKey: ['admin', resource], queryFn: () => config.api.list() })
  const save = useMutation({ mutationFn: (payload) => editing ? config.api.update(editing.id, payload) : config.api.create(payload), onSuccess: () => { toast.success(editing ? 'Đã cập nhật dữ liệu' : 'Đã tạo dữ liệu'); queryClient.invalidateQueries({ queryKey: ['admin', resource] }); clearForm() }, onError: (error) => toast.error(getApiError(error)) })
  const clearForm = () => { setEditing(null); setForm(initialValues(config)) }
  const selectEdit = (item) => { setEditing(item); setForm(Object.fromEntries(config.fields.map((field) => [field.name, item[field.name] ?? config.defaults?.[field.name] ?? '']))); window.scrollTo({ top: 0, behavior: 'smooth' }) }
  const submit = (event) => { event.preventDefault(); const payload = { ...form }; (config.numeric || []).forEach((key) => { payload[key] = toNumber(payload[key]) }); ['avatarUrl', 'birthDate', 'contactPhone'].forEach((key) => { if (key in payload && payload[key] === '') payload[key] = null }); save.mutate(payload) }
  if (!config) return null
  return <div><div className="admin-page-title"><div><span className="eyebrow">Quản lý danh mục</span><h1>{config.title}</h1><p>Tạo và cập nhật theo đúng endpoint backend. Tài nguyên này không có API xóa.</p></div></div><div className="admin-split"><form className="panel admin-form" onSubmit={submit}><div className="section-heading"><h2>{editing ? `Sửa ${config.singular}` : `Thêm ${config.singular}`}</h2>{editing && <button type="button" className="icon-button" onClick={clearForm} aria-label="Hủy chỉnh sửa"><RotateCcw /></button>}</div>{config.fields.map((field) => <label key={field.name}>{field.label}{field.type === 'textarea' ? <textarea rows="4" required={field.required} value={form[field.name]} onChange={(e) => setForm({ ...form, [field.name]: e.target.value })} /> : field.type === 'select' ? <select required={field.required} value={form[field.name]} onChange={(e) => setForm({ ...form, [field.name]: e.target.value })}>{field.options.map((option) => <option key={option} value={option}>{option}</option>)}</select> : <input type={field.type || 'text'} step={field.step} required={field.required} value={form[field.name]} onChange={(e) => setForm({ ...form, [field.name]: e.target.value })} />}</label>)}{resource === 'actors' && form.avatarUrl && <div className="image-preview"><SafeImage src={form.avatarUrl} alt="Xem trước ảnh diễn viên" /></div>}<button className="button button-primary full" disabled={save.isPending}>{editing ? <Save /> : <Plus />} {save.isPending ? 'Đang lưu...' : editing ? 'Lưu thay đổi' : 'Tạo mới'}</button></form><section className="panel table-panel"><div className="section-heading"><h2>Danh sách {config.title.toLowerCase()}</h2><span className="badge neutral">{query.data?.length || 0} mục</span></div>{query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !(query.data || []).length ? <EmptyState /> : <div className="table-scroll"><table><thead><tr>{config.columns.map(([, label]) => <th key={label}>{label}</th>)}<th>Thao tác</th></tr></thead><tbody>{query.data.map((item) => <tr key={item.id}>{config.columns.map(([key]) => <td key={key}>{key === 'avatarUrl' ? <SafeImage className="table-avatar" src={item[key]} alt={item.name} fallback="/poster-fallback.svg" /> : key === 'status' ? <StatusBadge value={item[key]} /> : key.toLowerCase().includes('date') ? formatDate(item[key]) : <span className="cell-clamp">{displayValue(item[key])}</span>}</td>)}<td><button className="button button-secondary button-sm" onClick={() => selectEdit(item)}><Edit3 /> Sửa</button></td></tr>)}</tbody></table></div>}</section></div></div>
}
