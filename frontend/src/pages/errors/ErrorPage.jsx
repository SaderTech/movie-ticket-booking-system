import { ArrowLeft, Home, LockKeyhole, Projector } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'

export function ErrorPage({ code = 404 }) {
  const navigate = useNavigate()
  const forbidden = Number(code) === 403
  return <div className="error-page"><div className="error-ambient" /><Link to="/" className="error-brand"><Projector /> MovieTicket</Link><div className={`error-visual ${forbidden ? 'forbidden' : ''}`}>{forbidden ? <LockKeyhole /> : <><div className="empty-screen"><span>NO SIGNAL</span></div><div className="empty-seats">{Array.from({ length: 5 }, (_, index) => <i key={index} />)}</div></>}<strong>{code}</strong></div><span className="eyebrow">{forbidden ? 'Khu vực giới hạn' : 'Suất chiếu không tồn tại'}</span><h1>{forbidden ? 'Bạn không có quyền truy cập' : 'Có vẻ bạn đã vào nhầm phòng chiếu'}</h1><p>{forbidden ? 'Khu vực này chỉ dành cho tài khoản có quyền quản trị được backend xác thực.' : 'Đường dẫn không tồn tại, đã được di chuyển hoặc bộ phim bạn tìm không còn ở đây.'}</p><div className="form-actions"><button className="button button-secondary" onClick={() => navigate(-1)}><ArrowLeft /> Quay lại</button><Link className="button button-primary" to="/"><Home /> Về trang chủ</Link></div></div>
}
