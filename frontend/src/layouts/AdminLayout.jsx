import { Activity, Armchair, Bell, BookOpenCheck, Building2, CalendarClock, ChevronRight, Clapperboard, Film, LayoutDashboard, LogOut, Logs, Mail, Menu, PanelLeftClose, PanelLeftOpen, Settings, Shapes, Theater, UserRound, UsersRound, Wrench, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { brandConfig } from '../config/appConfig'
import { useAuth } from '../hooks/useAuth'

const groups = [
  { label: 'Tổng quan', links: [['', 'Bảng điều khiển', LayoutDashboard]] },
  { label: 'Nội dung', links: [['movies', 'Phim', Film], ['genres', 'Thể loại', Shapes], ['actors', 'Diễn viên', UsersRound], ['directors', 'Đạo diễn', UserRound]] },
  { label: 'Vận hành rạp', links: [['cinemas', 'Rạp phim', Building2], ['halls', 'Phòng chiếu', Theater], ['seat-types', 'Loại ghế', Armchair], ['seats', 'Sơ đồ ghế', Armchair], ['hall-maintenances', 'Bảo trì phòng', Wrench], ['showtimes', 'Suất chiếu', CalendarClock]] },
  { label: 'Hệ thống', links: [['users', 'Người dùng', UsersRound], ['booking-settings', 'Cấu hình đặt vé', Settings], ['notifications', 'Gửi thông báo', Bell], ['notification-templates', 'Mẫu thông báo', Mail], ['notification-logs', 'Nhật ký gửi', Logs]] },
]

function SidebarContent({ collapsed, onNavigate, onToggle }) {
  return <><div className="admin-brand"><Link to="/" aria-label="Về MovieTicket"><span className="brand-mark"><Clapperboard /></span><span className="admin-brand-copy"><strong>{brandConfig.name}</strong><small>ADMIN CONSOLE</small></span></Link><button className="admin-collapse" onClick={onToggle} aria-label={collapsed ? 'Mở rộng thanh bên' : 'Thu gọn thanh bên'}>{collapsed ? <PanelLeftOpen /> : <PanelLeftClose />}</button></div><nav>{groups.map((group) => <div className="admin-nav-group" key={group.label}><span className="admin-nav-label">{group.label}</span>{group.links.map(([path, label, Icon]) => <NavLink title={collapsed ? label : undefined} key={label} end={!path} to={`/admin${path ? `/${path}` : ''}`} onClick={onNavigate}><Icon /><span>{label}</span><ChevronRight /></NavLink>)}</div>)}</nav><div className="admin-sidebar-footer"><Link className="admin-back" to="/"><BookOpenCheck /><span>Trang khách hàng</span></Link></div></>
}

export function AdminLayout() {
  const [open, setOpen] = useState(false)
  const [collapsed, setCollapsed] = useState(false)
  const location = useLocation()
  const { user, logout } = useAuth()
  useEffect(() => { setOpen(false) }, [location.pathname])
  useEffect(() => { const key = (event) => event.key === 'Escape' && setOpen(false); document.addEventListener('keydown', key); return () => document.removeEventListener('keydown', key) }, [])
  return <div className={`admin-shell ${collapsed ? 'sidebar-collapsed' : ''}`}><button className={`admin-overlay ${open ? 'visible' : ''}`} aria-label="Đóng trình đơn quản trị" onClick={() => setOpen(false)} /><aside className={`admin-sidebar ${open ? 'open' : ''}`}><button className="admin-mobile-close" onClick={() => setOpen(false)} aria-label="Đóng thanh bên"><X /></button><SidebarContent collapsed={collapsed} onNavigate={() => setOpen(false)} onToggle={() => setCollapsed(!collapsed)} /></aside><div className="admin-main"><header className="admin-topbar"><button className="icon-button admin-menu-button" onClick={() => setOpen(true)} aria-label="Mở trình đơn quản trị"><Menu /></button><div className="admin-breadcrumb"><span className="eyebrow"><Activity /> Hệ thống trực tuyến</span><strong>Khu vực quản trị</strong></div><div className="admin-user"><span className="avatar-initial">{(user?.username || 'A').charAt(0).toUpperCase()}</span><div><strong>{user?.username || 'Administrator'}</strong><small>{user?.email || 'Quản trị viên'}</small></div><button className="icon-button" onClick={logout} aria-label="Đăng xuất"><LogOut /></button></div></header><main className="admin-content"><Outlet /></main></div></div>
}
