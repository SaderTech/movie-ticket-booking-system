import { LoaderCircle } from 'lucide-react'
import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { PageTransition } from '../components/common/Motion'
import { AdminRoute } from './AdminRoute'
import { ProtectedRoute } from './ProtectedRoute'

const lazyNamed = (loader, name) => lazy(() => loader().then((module) => ({ default: module[name] })))
const PublicLayout = lazyNamed(() => import('../layouts/PublicLayout'), 'PublicLayout')
const AccountLayout = lazyNamed(() => import('../layouts/AccountLayout'), 'AccountLayout')
const AdminLayout = lazyNamed(() => import('../layouts/AdminLayout'), 'AdminLayout')
const HomePage = lazyNamed(() => import('../pages/public/HomePage'), 'HomePage')
const MoviesPage = lazyNamed(() => import('../pages/public/MoviesPage'), 'MoviesPage')
const MovieDetailPage = lazyNamed(() => import('../pages/public/MovieDetailPage'), 'MovieDetailPage')
const CinemasPage = lazyNamed(() => import('../pages/public/CinemasPage'), 'CinemasPage')
const CinemaDetailPage = lazyNamed(() => import('../pages/public/CinemaDetailPage'), 'CinemaDetailPage')
const ShowtimesPage = lazyNamed(() => import('../pages/public/ShowtimesPage'), 'ShowtimesPage')
const ProfilePage = lazyNamed(() => import('../pages/account/ProfilePage'), 'ProfilePage')
const BookingsPage = lazyNamed(() => import('../pages/account/BookingsPage'), 'BookingsPage')
const BookingDetailPage = lazyNamed(() => import('../pages/account/BookingDetailPage'), 'BookingDetailPage')
const LoginPage = lazyNamed(() => import('../pages/auth/LoginPage'), 'LoginPage')
const RegisterPage = lazyNamed(() => import('../pages/auth/RegisterPage'), 'RegisterPage')
const SeatSelectionPage = lazyNamed(() => import('../pages/booking/SeatSelectionPage'), 'SeatSelectionPage')
const CheckoutPage = lazyNamed(() => import('../pages/booking/CheckoutPage'), 'CheckoutPage')
const BookingSuccessPage = lazyNamed(() => import('../pages/booking/BookingSuccessPage'), 'BookingSuccessPage')
const PaymentResultPage = lazyNamed(() => import('../pages/booking/PaymentResultPage'), 'PaymentResultPage')
const AdminDashboard = lazyNamed(() => import('../pages/admin/AdminDashboard'), 'AdminDashboard')
const AdminMoviesPage = lazyNamed(() => import('../pages/admin/AdminMoviesPage'), 'AdminMoviesPage')
const AdminResourcePage = lazyNamed(() => import('../pages/admin/AdminResourcePage'), 'AdminResourcePage')
const AdminHallsPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminHallsPage')
const AdminSeatsPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminSeatsPage')
const AdminMaintenancesPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminMaintenancesPage')
const AdminShowtimesPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminShowtimesPage')
const AdminUsersPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminUsersPage')
const AdminBookingSettingsPage = lazyNamed(() => import('../pages/admin/AdminOperationalPages'), 'AdminBookingSettingsPage')
const AdminNotificationsPage = lazyNamed(() => import('../pages/admin/AdminNotificationPages'), 'AdminNotificationsPage')
const AdminNotificationTemplatesPage = lazyNamed(() => import('../pages/admin/AdminNotificationPages'), 'AdminNotificationTemplatesPage')
const AdminNotificationLogsPage = lazyNamed(() => import('../pages/admin/AdminNotificationPages'), 'AdminNotificationLogsPage')
const ErrorPage = lazyNamed(() => import('../pages/errors/ErrorPage'), 'ErrorPage')

function RouteFallback() {
  return <div className="route-fallback" role="status"><LoaderCircle className="spin" /><span>Đang mở phòng chiếu…</span></div>
}

export function AppRoutes() {
  const location = useLocation()
  return <Suspense fallback={<RouteFallback />}><PageTransition key={location.pathname}><Routes location={location}>
    <Route element={<PublicLayout />}>
      <Route index element={<HomePage />} />
      <Route path="movies" element={<MoviesPage />} />
      <Route path="movies/:id" element={<MovieDetailPage />} />
      <Route path="cinemas" element={<CinemasPage />} />
      <Route path="cinemas/:id" element={<CinemaDetailPage />} />
      <Route path="showtimes" element={<ShowtimesPage />} />
      <Route path="account" element={<ProtectedRoute><AccountLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="profile" replace />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="bookings" element={<BookingsPage />} />
        <Route path="bookings/:bookingCode" element={<BookingDetailPage />} />
      </Route>
    </Route>
    <Route path="login" element={<LoginPage />} />
    <Route path="register" element={<RegisterPage />} />
    <Route path="booking/:showtimeId/seats" element={<ProtectedRoute><SeatSelectionPage /></ProtectedRoute>} />
    <Route path="booking/checkout" element={<ProtectedRoute><CheckoutPage /></ProtectedRoute>} />
    <Route path="booking/success/:bookingCode" element={<ProtectedRoute><BookingSuccessPage /></ProtectedRoute>} />
    <Route path="payment-result" element={<PaymentResultPage />} />
    <Route path="admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
      <Route index element={<AdminDashboard />} />
      <Route path="movies" element={<AdminMoviesPage />} />
      <Route path="genres" element={<AdminResourcePage resource="genres" />} />
      <Route path="actors" element={<AdminResourcePage resource="actors" />} />
      <Route path="directors" element={<AdminResourcePage resource="directors" />} />
      <Route path="cinemas" element={<AdminResourcePage resource="cinemas" />} />
      <Route path="halls" element={<AdminHallsPage />} />
      <Route path="seat-types" element={<AdminResourcePage resource="seat-types" />} />
      <Route path="seats" element={<AdminSeatsPage />} />
      <Route path="hall-maintenances" element={<AdminMaintenancesPage />} />
      <Route path="showtimes" element={<AdminShowtimesPage />} />
      <Route path="users" element={<AdminUsersPage />} />
      <Route path="booking-settings" element={<AdminBookingSettingsPage />} />
      <Route path="notifications" element={<AdminNotificationsPage />} />
      <Route path="notification-templates" element={<AdminNotificationTemplatesPage />} />
      <Route path="notification-logs" element={<AdminNotificationLogsPage />} />
    </Route>
    <Route path="403" element={<ErrorPage code={403} />} />
    <Route path="*" element={<ErrorPage code={404} />} />
  </Routes></PageTransition></Suspense>
}
