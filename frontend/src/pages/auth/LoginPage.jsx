import { zodResolver } from '@hookform/resolvers/zod'
import { ArrowLeft, Clapperboard, Eye, EyeOff, LogIn, ShieldCheck, Ticket } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../../hooks/useAuth'
import { getApiError } from '../../utils/apiError'
import { LoadingSpinner } from '../../components/common/DesignSystem'

const schema = z.object({ email: z.string().min(1, 'Vui lòng nhập email').email('Email không hợp lệ'), password: z.string().min(1, 'Vui lòng nhập mật khẩu') })

export function LoginPage() {
  const [show, setShow] = useState(false)
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({ resolver: zodResolver(schema) })
  if (isAuthenticated) return <Navigate to="/" replace />
  const onSubmit = async (values) => { try { await login(values); toast.success('Đăng nhập thành công'); const target = params.get('redirect') || location.state?.from || '/'; navigate(target, { replace: true }) } catch (error) { toast.error(getApiError(error, 'Đăng nhập thất bại.')) } }
  return <div className="auth-page"><section className="auth-aside"><div className="auth-beams" /><Link className="brand" to="/"><span className="brand-mark"><Clapperboard /></span> MovieTicket</Link><div className="auth-story"><span className="eyebrow">Chào mừng trở lại</span><h1>Câu chuyện tiếp theo đang chờ bạn.</h1><p>Đăng nhập để giữ vị trí đẹp nhất và mang vé điện tử theo bên mình.</p><div className="auth-benefits"><span><Ticket /> Giữ ghế trực quan</span><span><ShieldCheck /> Thanh toán được xác minh</span></div></div><small>MovieTicket · Mỗi suất chiếu, một thế giới mới.</small></section><section className="auth-form-wrap"><Link className="auth-back" to="/"><ArrowLeft /> Về trang chủ</Link><form className="auth-form" onSubmit={handleSubmit(onSubmit)} noValidate><div className="auth-form-title"><span className="eyebrow">Tài khoản MovieTicket</span><h1>Đăng nhập</h1><p>Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link></p></div><label>Email<input type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} aria-invalid={!!errors.email} />{errors.email && <small className="field-error">{errors.email.message}</small>}</label><label>Mật khẩu<div className="password-field"><input type={show ? 'text' : 'password'} autoComplete="current-password" placeholder="Nhập mật khẩu" {...register('password')} aria-invalid={!!errors.password} /><button type="button" onClick={() => setShow(!show)} aria-label={show ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}>{show ? <EyeOff /> : <Eye />}</button></div>{errors.password && <small className="field-error">{errors.password.message}</small>}</label><button className="button button-primary button-lg full" disabled={isSubmitting}>{isSubmitting ? <><LoadingSpinner /> Đang đăng nhập…</> : <><LogIn /> Đăng nhập</>}</button><p className="form-note">Hệ thống hiện không có API quên mật khẩu hoặc đăng nhập mạng xã hội.</p></form></section></div>
}
