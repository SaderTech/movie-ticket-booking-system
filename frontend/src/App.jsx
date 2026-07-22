import { Toaster } from 'react-hot-toast'
import { AppRoutes } from './routes/AppRoutes'

export default function App() {
  return <><AppRoutes /><Toaster position="top-right" gutter={10} toastOptions={{ duration: 3600, className: 'cinema-toast', success: { iconTheme: { primary: '#34d399', secondary: '#081019' } }, error: { iconTheme: { primary: '#fb7185', secondary: '#081019' } } }} /></>
}
