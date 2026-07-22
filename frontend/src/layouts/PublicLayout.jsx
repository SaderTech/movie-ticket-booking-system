import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Building2, CalendarDays, ChevronDown, Clapperboard, Film, Home, LogIn, LogOut, Menu, ShieldCheck, Ticket, UserRound } from 'lucide-react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { cinemaApi } from '../api/cinemaApi'
import { Drawer, Dropdown } from '../components/common/Overlay'
import { appConfig, brandConfig, selectedCinemaKey } from '../config/appConfig'
import { useAuth } from '../hooks/useAuth'

const navigation = [
  ['/', 'Trang chủ', Home],
  ['/movies', 'Phim', Film],
  ['/cinemas', 'Rạp phim', Building2],
  ['/showtimes', 'Lịch chiếu', CalendarDays],
]

function NavigationLinks({ onNavigate, mobile = false }) {
  return <nav className={mobile ? 'drawer-nav' : 'main-nav'} aria-label="Điều hướng chính">{navigation.map(([path, label, Icon]) => <NavLink end={path === '/'} to={path} onClick={onNavigate} key={path}><Icon /><span>{label}</span></NavLink>)}</nav>
}

export function PublicLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [accountOpen, setAccountOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)
  const [cinemaId, setCinemaId] = useState(() => localStorage.getItem(selectedCinemaKey) || '')
  const location = useLocation()
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const cinemas = useQuery({ queryKey: ['cinemas', 'ACTIVE'], queryFn: () => cinemaApi.list('ACTIVE'), staleTime: 300000 })
  const selectedCinema = (cinemas.data || []).find((cinema) => String(cinema.id) === String(cinemaId))

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 24)
    onScroll()
    window.addEventListener('scroll', onScroll, { passive: true })
    return () => window.removeEventListener('scroll', onScroll)
  }, [])
  useEffect(() => { setMobileOpen(false); setAccountOpen(false); window.scrollTo({ top: 0, behavior: 'instant' }) }, [location.pathname])

  const selectCinema = (event) => {
    setCinemaId(event.target.value)
    if (event.target.value) localStorage.setItem(selectedCinemaKey, event.target.value)
    else localStorage.removeItem(selectedCinemaKey)
  }
  const initial = (user?.username || user?.email || 'M').charAt(0).toUpperCase()
  const solid = scrolled || location.pathname !== '/'

  return (
    <div className="app-shell">
      <header className={`site-header ${solid ? 'solid' : 'transparent'}`}>
        <div className="container header-inner">
          <Link to="/" className="brand" aria-label={`${brandConfig.name} - Trang chủ`}><span className="brand-mark"><Clapperboard /></span><span>{brandConfig.name}</span></Link>
          <NavigationLinks />
          <label className="cinema-picker"><Building2 /><span><small>Rạp của bạn</small><select value={cinemaId} onChange={selectCinema} aria-label="Chọn rạp ưu tiên"><option value="">Tất cả rạp</option>{(cinemas.data || []).map((cinema) => <option value={cinema.id} key={cinema.id}>{cinema.name}</option>)}</select></span></label>
          <div className="header-actions">
            {isAuthenticated ? <Dropdown open={accountOpen} onClose={() => setAccountOpen(false)} className="account-dropdown"><button className="account-trigger" type="button" onClick={() => setAccountOpen(!accountOpen)} aria-expanded={accountOpen} aria-haspopup="menu"><span className="avatar-initial">{initial}</span><span className="account-name">{user?.username || 'Tài khoản'}</span><ChevronDown /></button>{accountOpen && <div className="dropdown-menu" role="menu"><div className="dropdown-user"><span className="avatar-initial large">{initial}</span><div><strong>{user?.username || 'MovieTicket'}</strong><small>{user?.email}</small></div></div><Link to="/account/profile" role="menuitem"><UserRound /> Hồ sơ</Link><Link to="/account/bookings" role="menuitem"><Ticket /> Vé của tôi</Link>{isAdmin && <Link to="/admin" role="menuitem"><ShieldCheck /> Trang quản trị</Link>}<button type="button" onClick={logout} role="menuitem"><LogOut /> Đăng xuất</button></div>}</Dropdown> : <Link className="login-link" to="/login"><LogIn /> Đăng nhập</Link>}
            <Link className="button button-primary buy-ticket" to="/showtimes"><Ticket /> Mua vé</Link>
            <button className="mobile-menu icon-button" onClick={() => setMobileOpen(true)} aria-label="Mở trình đơn" aria-expanded={mobileOpen}><Menu /></button>
          </div>
        </div>
      </header>

      <Drawer open={mobileOpen} onClose={() => setMobileOpen(false)} title="MovieTicket">
        <div className="drawer-brand"><span className="brand-mark"><Clapperboard /></span><div><strong>{brandConfig.name}</strong><small>{brandConfig.tagline}</small></div></div>
        <label className="drawer-cinema"><span>Rạp ưu tiên</span><select value={cinemaId} onChange={selectCinema}><option value="">Tất cả rạp</option>{(cinemas.data || []).map((cinema) => <option value={cinema.id} key={cinema.id}>{cinema.name}</option>)}</select></label>
        <NavigationLinks mobile onNavigate={() => setMobileOpen(false)} />
        <div className="drawer-account">{isAuthenticated ? <><Link to="/account/profile" onClick={() => setMobileOpen(false)}><UserRound /> Hồ sơ cá nhân</Link><Link to="/account/bookings" onClick={() => setMobileOpen(false)}><Ticket /> Vé của tôi</Link>{isAdmin && <Link to="/admin" onClick={() => setMobileOpen(false)}><ShieldCheck /> Quản trị</Link>}<button onClick={logout}><LogOut /> Đăng xuất</button></> : <Link className="button button-primary full" to="/login" onClick={() => setMobileOpen(false)}><LogIn /> Đăng nhập</Link>}</div>
      </Drawer>

      <main><Outlet context={{ selectedCinemaId: cinemaId, selectedCinema }} /></main>
      <footer className="site-footer"><div className="footer-glow" aria-hidden="true" /><div className="container footer-grid"><div className="footer-brand"><Link to="/" className="brand"><span className="brand-mark"><Clapperboard /></span>{brandConfig.name}</Link><p>{brandConfig.description}</p><span className="footer-caption">{brandConfig.tagline}</span></div><div><h3>Khám phá</h3><Link to="/movies">Phim đang chiếu</Link><Link to="/showtimes">Lịch chiếu hôm nay</Link><Link to="/cinemas">Hệ thống rạp</Link></div><div><h3>Tài khoản</h3><Link to="/account/profile">Hồ sơ cá nhân</Link><Link to="/account/bookings">Vé của tôi</Link><Link to="/login">Đăng nhập</Link></div><div><h3>Kết nối</h3><p>Frontend kết nối an toàn duy nhất qua API Gateway.</p><span className="gateway-chip"><span /> localhost:8080</span></div></div><div className="container footer-bottom"><span>© {new Date().getFullYear()} {appConfig.brandName}. All rights reserved.</span><span>Made for the big screen.</span></div></footer>
    </div>
  )
}
