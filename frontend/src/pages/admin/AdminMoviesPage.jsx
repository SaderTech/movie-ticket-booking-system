import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Edit3, Film, Play, Plus, Save, Square, X } from 'lucide-react'
import { useCallback, useState } from 'react'
import toast from 'react-hot-toast'
import { movieApi } from '../../api/movieApi'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { Modal } from '../../components/common/Overlay'
import { SafeImage } from '../../components/common/SafeImage'
import { StatusBadge } from '../../components/common/StatusBadge'
import { getApiError } from '../../utils/apiError'
import { formatDate, toNumber } from '../../utils/formatters'

const emptyMovie = {
  title: '',
  description: '',
  durationMinutes: '',
  trailerUrl: '',
  posterUrl: '',
  releaseDate: '',
  ageRating: 'P',
  status: 'COMING_SOON',
  genreIds: [],
  directorIds: [],
}

function ReferencePicker({ legend, placeholder, addLabel, items = [], selectedIds = [], onChange }) {
  const [value, setValue] = useState('')
  const selected = selectedIds.map((id) => items.find((item) => String(item.id) === String(id)) || { id, name: `#${id}` })
  const available = items.filter((item) => !selectedIds.some((id) => String(id) === String(item.id)))

  const add = () => {
    if (!value) return
    onChange([...selectedIds, value])
    setValue('')
  }

  return (
    <fieldset className="actor-builder reference-builder">
      <legend>{legend}</legend>
      <div className="reference-add">
        <select value={value} onChange={(event) => setValue(event.target.value)} aria-label={placeholder}>
          <option value="">{placeholder}</option>
          {available.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}
        </select>
        <button type="button" className="button button-secondary" disabled={!value} onClick={add}><Plus /> {addLabel}</button>
      </div>
      <div className="actor-tags selection-tags">
        {selected.map((item) => (
          <span key={item.id}>{item.name}<button type="button" onClick={() => onChange(selectedIds.filter((id) => String(id) !== String(item.id)))} aria-label={`Bỏ ${item.name}`}><X /></button></span>
        ))}
      </div>
    </fieldset>
  )
}

export function AdminMoviesPage() {
  const queryClient = useQueryClient()
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState({ ...emptyMovie })
  const [actorAssignments, setActorAssignments] = useState([])
  const [actorId, setActorId] = useState('')
  const [roleName, setRoleName] = useState('')

  const movies = useQuery({ queryKey: ['admin', 'movies'], queryFn: () => movieApi.list() })
  const genres = useQuery({ queryKey: ['admin', 'genres'], queryFn: movieApi.genres.list, staleTime: 120000 })
  const actors = useQuery({ queryKey: ['admin', 'actors'], queryFn: movieApi.actors.list, staleTime: 120000 })
  const directors = useQuery({ queryKey: ['admin', 'directors'], queryFn: movieApi.directors.list, staleTime: 120000 })

  const clearForm = useCallback(() => {
    setEditing(null)
    setForm({ ...emptyMovie })
    setActorAssignments([])
    setActorId('')
    setRoleName('')
  }, [])

  const closeForm = useCallback(() => {
    setFormOpen(false)
    clearForm()
  }, [clearForm])

  const openCreate = useCallback(() => {
    clearForm()
    setFormOpen(true)
  }, [clearForm])

  const save = useMutation({
    mutationFn: (payload) => editing ? movieApi.update(editing.id, payload) : movieApi.create(payload),
    onSuccess: () => {
      toast.success(editing ? 'Đã cập nhật phim' : 'Đã tạo phim')
      queryClient.invalidateQueries({ queryKey: ['admin', 'movies'] })
      closeForm()
    },
    onError: (error) => toast.error(getApiError(error)),
  })

  const lifecycle = useMutation({
    mutationFn: ({ id, action }) => action === 'start' ? movieApi.start(id) : movieApi.end(id),
    onSuccess: () => {
      toast.success('Đã cập nhật trạng thái phim')
      queryClient.invalidateQueries({ queryKey: ['admin', 'movies'] })
    },
    onError: (error) => toast.error(getApiError(error)),
  })

  const edit = (movie) => {
    setEditing(movie)
    setForm({
      title: movie.title || '',
      description: movie.description || '',
      durationMinutes: movie.durationMinutes || '',
      trailerUrl: movie.trailerUrl || '',
      posterUrl: movie.posterUrl || '',
      releaseDate: movie.releaseDate || '',
      ageRating: movie.ageRating || 'P',
      status: movie.status || 'COMING_SOON',
      genreIds: (movie.genres || []).map((item) => String(item.id)),
      directorIds: (movie.directors || []).map((item) => String(item.id)),
    })
    setActorAssignments((movie.actors || []).map((item) => ({ actorId: String(item.id), roleName: item.roleName || '' })))
    setActorId('')
    setRoleName('')
    setFormOpen(true)
  }

  const addActor = () => {
    if (!actorId || !roleName.trim()) return toast.error('Chọn diễn viên và nhập vai diễn')
    if (actorAssignments.some((item) => item.actorId === actorId)) return toast.error('Diễn viên đã được thêm')
    setActorAssignments([...actorAssignments, { actorId, roleName: roleName.trim() }])
    setActorId('')
    setRoleName('')
  }

  const submit = (event) => {
    event.preventDefault()
    if (!form.genreIds.length || !form.directorIds.length || !actorAssignments.length) {
      return toast.error('Phim cần ít nhất một thể loại, diễn viên và đạo diễn')
    }
    save.mutate({
      ...form,
      posterUrl: form.posterUrl || null,
      durationMinutes: toNumber(form.durationMinutes),
      genreIds: form.genreIds.map(Number),
      directorIds: form.directorIds.map(Number),
      actors: actorAssignments.map((item) => ({ actorId: Number(item.actorId), roleName: item.roleName })),
    })
  }

  const loadingRefs = genres.isLoading || actors.isLoading || directors.isLoading

  return (
    <div>
      <div className="admin-page-title">
        <div><span className="eyebrow"><Film /> Nội dung điện ảnh</span><h1>Quản lý phim</h1><p>Danh sách phim và trạng thái phát hành. Chọn “Thêm phim” hoặc “Sửa” để mở biểu mẫu thông tin.</p></div>
        <button className="button button-primary" onClick={openCreate}><Plus /> Thêm phim</button>
      </div>

      <Modal open={formOpen} onClose={save.isPending ? undefined : closeForm} title={editing ? `Chỉnh sửa “${editing.title}”` : 'Thêm phim mới'} size="xl">
        <form className="admin-movie-form" onSubmit={submit}>
          <div className="movie-form-layout">
            <div className="form-grid two">
              <label>Tên phim<input required maxLength="255" value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} /></label>
              <label>Thời lượng (phút)<input required type="number" min="1" max="600" value={form.durationMinutes} onChange={(event) => setForm({ ...form, durationMinutes: event.target.value })} /></label>
              <label>Ngày khởi chiếu<input required type="date" value={form.releaseDate} onChange={(event) => setForm({ ...form, releaseDate: event.target.value })} /></label>
              <label>Độ tuổi<select value={form.ageRating} onChange={(event) => setForm({ ...form, ageRating: event.target.value })}>{['P', 'C13', 'C16', 'C18'].map((item) => <option key={item}>{item}</option>)}</select></label>
              <label>Trạng thái<select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>{['COMING_SOON', 'NOW_SHOWING', 'ENDED'].map((item) => <option key={item}>{item}</option>)}</select></label>
              <label>URL trailer<input required type="url" maxLength="500" value={form.trailerUrl} onChange={(event) => setForm({ ...form, trailerUrl: event.target.value })} /></label>
              <label className="full-span">URL poster<input type="url" maxLength="500" value={form.posterUrl} onChange={(event) => setForm({ ...form, posterUrl: event.target.value })} /></label>
              <label className="full-span">Mô tả<textarea rows="5" maxLength="5000" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
            </div>
            <div className="poster-admin-preview"><SafeImage src={form.posterUrl} alt="Xem trước poster" /></div>
          </div>

          <div className="form-grid two reference-fields">
            <ReferencePicker legend="Thể loại" placeholder="Chọn thể loại" addLabel="Thêm thể loại" items={genres.data || []} selectedIds={form.genreIds} onChange={(genreIds) => setForm((current) => ({ ...current, genreIds }))} />
            <ReferencePicker legend="Đạo diễn" placeholder="Chọn đạo diễn" addLabel="Thêm đạo diễn" items={directors.data || []} selectedIds={form.directorIds} onChange={(directorIds) => setForm((current) => ({ ...current, directorIds }))} />
          </div>

          <fieldset className="actor-builder">
            <legend>Diễn viên và vai diễn</legend>
            <div className="actor-add">
              <select value={actorId} onChange={(event) => setActorId(event.target.value)} aria-label="Chọn diễn viên">
                <option value="">Chọn diễn viên</option>
                {(actors.data || []).filter((actor) => !actorAssignments.some((item) => item.actorId === String(actor.id))).map((actor) => <option value={actor.id} key={actor.id}>{actor.name}</option>)}
              </select>
              <input value={roleName} onChange={(event) => setRoleName(event.target.value)} placeholder="Tên vai diễn" aria-label="Tên vai diễn" />
              <button type="button" className="button button-secondary" onClick={addActor}><Plus /> Thêm diễn viên</button>
            </div>
            <div className="actor-tags">
              {actorAssignments.map((item) => {
                const actor = (actors.data || []).find((entry) => String(entry.id) === item.actorId)
                return <span key={item.actorId}>{actor?.name || `#${item.actorId}`} — {item.roleName}<button type="button" onClick={() => setActorAssignments(actorAssignments.filter((entry) => entry.actorId !== item.actorId))} aria-label="Bỏ diễn viên"><X /></button></span>
              })}
            </div>
          </fieldset>

          <div className="form-actions movie-form-actions">
            <button type="button" className="button button-secondary" disabled={save.isPending} onClick={closeForm}>Hủy</button>
            <button className="button button-primary" disabled={save.isPending || loadingRefs}><Save /> {save.isPending ? 'Đang lưu...' : editing ? 'Cập nhật phim' : 'Tạo phim'}</button>
          </div>
        </form>
      </Modal>

      <section className="panel table-panel admin-list-section">
        <div className="section-heading"><h2>Danh sách phim</h2><span className="badge neutral">{movies.data?.length || 0} phim</span></div>
        {movies.isLoading ? <SkeletonGrid count={5} compact /> : movies.isError ? <ErrorState message={getApiError(movies.error)} onRetry={movies.refetch} /> : !(movies.data || []).length ? <EmptyState title="Chưa có phim" /> : (
          <div className="table-scroll">
            <table>
              <thead><tr><th>Poster</th><th>Phim</th><th>Ngày chiếu</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
              <tbody>
                {movies.data.map((movie) => (
                  <tr key={movie.id}>
                    <td><SafeImage className="table-poster" src={movie.posterUrl} alt={movie.title} /></td>
                    <td><strong>{movie.title}</strong><small>{movie.durationMinutes} phút · {movie.ageRating}</small></td>
                    <td>{formatDate(movie.releaseDate)}</td>
                    <td><StatusBadge value={movie.status} /></td>
                    <td><div className="row-actions"><button className="button button-secondary button-sm" onClick={() => edit(movie)}><Edit3 /> Sửa</button>{movie.status === 'COMING_SOON' && <button className="button button-success button-sm" onClick={() => lifecycle.mutate({ id: movie.id, action: 'start' })}><Play /> Bắt đầu</button>}{movie.status === 'NOW_SHOWING' && <button className="button button-warning button-sm" onClick={() => lifecycle.mutate({ id: movie.id, action: 'end' })}><Square /> Kết thúc</button>}</div></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
