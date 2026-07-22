import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Clapperboard, SlidersHorizontal } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { movieApi } from '../../api/movieApi'
import { MovieGrid } from '../../components/movie/MovieGrid'
import { ErrorState } from '../../components/common/AsyncState'
import { getApiError } from '../../utils/apiError'
import { appConfig } from '../../config/appConfig'
import { PageHero, SearchInput, Tabs } from '../../components/common/DesignSystem'
import { Reveal } from '../../components/common/Motion'

const tabs = [['', 'Tất cả'], ['NOW_SHOWING', 'Đang chiếu'], ['COMING_SOON', 'Sắp chiếu']]

export function MoviesPage() {
  const [params, setParams] = useSearchParams()
  const [keyword, setKeyword] = useState(params.get('q') || '')
  const [debounced, setDebounced] = useState(keyword.trim())
  const status = params.get('status') || ''
  useEffect(() => { const timer = setTimeout(() => setDebounced(keyword.trim()), appConfig.movieSearchDebounceMs); return () => clearTimeout(timer) }, [keyword])
  useEffect(() => {
    const next = new URLSearchParams(params)
    if (debounced) next.set('q', debounced); else next.delete('q')
    setParams(next, { replace: true })
  }, [debounced])
  const movies = useQuery({
    queryKey: ['movies', debounced ? 'search' : status || 'all', debounced],
    queryFn: () => debounced ? movieApi.search(debounced) : movieApi.list(status || undefined),
    staleTime: debounced ? 0 : 60000,
    retry: (count, error) => error?.response?.status !== 429 && count < 1,
  })
  const changeStatus = (value) => { const next = new URLSearchParams(params); if (value) next.set('status', value); else next.delete('status'); setParams(next) }
  return <div><PageHero eyebrow={<><Clapperboard /> Danh mục điện ảnh</>} title="Khám phá phim" description="Từ bom tấn hành động đến những câu chuyện chạm cảm xúc—suất chiếu tiếp theo của bạn bắt đầu tại đây." /><section className="container page-section movies-catalog"><Reveal className="filter-panel"><SearchInput value={keyword} onChange={(event) => setKeyword(event.target.value)} onClear={() => setKeyword('')} placeholder="Tìm theo tên phim…" label="Tìm phim" /><div className="filter-tabs"><span><SlidersHorizontal /> Trạng thái</span><Tabs items={tabs} value={status} onChange={changeStatus} label="Lọc trạng thái phim" /></div></Reveal><div className="results-caption"><span>{movies.isLoading ? 'Đang tìm những thước phim phù hợp…' : `${movies.data?.length || 0} phim được tìm thấy`}</span>{debounced && <strong>Kết quả cho “{debounced}”</strong>}</div>{movies.isError ? <ErrorState message={getApiError(movies.error)} onRetry={movies.refetch} /> : <MovieGrid movies={movies.data || []} isLoading={movies.isLoading} emptyTitle={debounced ? `Không tìm thấy “${debounced}”` : 'Chưa có phim trong danh mục'} />}</section></div>
}
