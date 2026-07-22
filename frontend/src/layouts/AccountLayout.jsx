import { ChevronRight, Sparkles, Ticket, UserRound } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export function AccountLayout() {
  const { user } = useAuth()
  const initial = (user?.username || user?.email || 'M').charAt(0).toUpperCase()
  return <div className="account-page"><div className="container account-header"><div className="account-avatar">{initial}</div><div><span className="eyebrow"><Sparkles /> Thành viên MovieTicket</span><h1>Xin chào, {user?.username || 'khán giả'}</h1><p>Quản lý hồ sơ và hành trình điện ảnh của bạn.</p></div></div><div className="container account-layout"><aside className="account-nav"><span className="account-nav-label">Tài khoản</span><NavLink to="/account/profile"><UserRound /><span>Hồ sơ cá nhân</span><ChevronRight /></NavLink><NavLink to="/account/bookings"><Ticket /><span>Vé của tôi</span><ChevronRight /></NavLink></aside><section className="account-content"><Outlet /></section></div></div>
}
