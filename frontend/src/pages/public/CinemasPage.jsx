import { useQuery } from '@tanstack/react-query'
import { Building2, MapPin } from 'lucide-react'
import { useMemo, useState } from 'react'
import { cinemaApi } from '../../api/cinemaApi'
import { CinemaCard } from '../../components/cinema/CinemaCard'
import { EmptyState, ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { PageHero, SearchInput, SectionTitle } from '../../components/common/DesignSystem'
import { StaggerGroup, StaggerItem } from '../../components/common/Motion'
import { getApiError } from '../../utils/apiError'

export function CinemasPage() {
  const [keyword, setKeyword] = useState('')
  const query = useQuery({ queryKey: ['cinemas', 'ACTIVE'], queryFn: () => cinemaApi.list('ACTIVE'), staleTime: 300000 })
  const filtered = useMemo(() => {
    const normalized = keyword.trim().toLocaleLowerCase('vi')
    if (!normalized) return query.data || []
    return (query.data || []).filter((cinema) => `${cinema.name} ${cinema.city} ${cinema.address}`.toLocaleLowerCase('vi').includes(normalized))
  }, [keyword, query.data])
  return <div><PageHero eyebrow={<><Building2 /> Hệ thống MovieTicket</>} title="Điểm đến của những câu chuyện" description="Khám phá hệ thống rạp, không gian phòng chiếu và lịch phim đang diễn ra gần bạn." /><section className="container page-section cinemas-page"><div className="cinema-toolbar"><SectionTitle eyebrow={<><MapPin /> Toàn bộ địa điểm</>} title="Chọn rạp của bạn" description="Tìm theo tên rạp, thành phố hoặc địa chỉ." /><SearchInput value={keyword} onChange={(event) => setKeyword(event.target.value)} onClear={() => setKeyword('')} placeholder="Tìm rạp hoặc thành phố…" label="Tìm rạp" /></div>{query.isLoading ? <SkeletonGrid count={4} compact /> : query.isError ? <ErrorState message={getApiError(query.error)} onRetry={query.refetch} /> : !filtered.length ? <EmptyState title={keyword ? `Không tìm thấy “${keyword}”` : 'Chưa có rạp hoạt động'} message={keyword ? 'Hãy thử một tên rạp hoặc thành phố khác.' : 'Danh sách rạp sẽ xuất hiện khi backend có dữ liệu.'} /> : <StaggerGroup className="cinema-grid">{filtered.map((cinema, index) => <StaggerItem key={cinema.id}><CinemaCard cinema={cinema} index={index} /></StaggerItem>)}</StaggerGroup>}</section></div>
}
