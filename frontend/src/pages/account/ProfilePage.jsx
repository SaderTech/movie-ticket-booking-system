import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Image, Save, UserRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { z } from 'zod'
import { userApi } from '../../api/userApi'
import { ErrorState, SkeletonGrid } from '../../components/common/AsyncState'
import { SafeImage } from '../../components/common/SafeImage'
import { getApiError } from '../../utils/apiError'
import { LoadingSpinner } from '../../components/common/DesignSystem'

const schema = z.object({ fullName: z.string().max(100, 'Họ tên tối đa 100 ký tự').optional(), phone: z.string().regex(/^(0|\+84)[0-9]{9}$/, 'Số điện thoại Việt Nam không hợp lệ').or(z.literal('')), avatar: z.string().url('Avatar phải là URL hợp lệ').max(255, 'URL tối đa 255 ký tự').or(z.literal('')) })

export function ProfilePage() {
  const queryClient = useQueryClient()
  const [preview, setPreview] = useState('')
  const profile = useQuery({ queryKey: ['profile'], queryFn: userApi.me })
  const { register, reset, handleSubmit, watch, formState: { errors } } = useForm({ resolver: zodResolver(schema), defaultValues: { fullName: '', phone: '', avatar: '' } })
  const avatar = watch('avatar')
  useEffect(() => { if (profile.data) { reset({ fullName: profile.data.fullName || '', phone: profile.data.phone || '', avatar: profile.data.avatar || '' }); setPreview(profile.data.avatar || '') } }, [profile.data, reset])
  useEffect(() => { const timer = setTimeout(() => setPreview(avatar || ''), 350); return () => clearTimeout(timer) }, [avatar])
  const update = useMutation({ mutationFn: (payload) => userApi.update(profile.data.id, payload), onSuccess: (data) => { toast.success('Đã cập nhật hồ sơ'); queryClient.setQueryData(['profile'], data) }, onError: (error) => toast.error(getApiError(error)) })
  if (profile.isLoading) return <SkeletonGrid count={2} compact />
  if (profile.isError) return <ErrorState message={getApiError(profile.error)} onRetry={profile.refetch} />
  return <div><div className="page-title compact"><span className="eyebrow"><UserRound /> Thông tin cá nhân</span><h1>Hồ sơ của bạn</h1><p>Cập nhật thông tin được backend hỗ trợ. Avatar chỉ nhận URL ảnh.</p></div><form className="panel profile-form" onSubmit={handleSubmit((values) => update.mutate({ ...values, phone: values.phone || null }))}><div className="avatar-editor"><div className="avatar-preview-shell"><SafeImage src={preview} alt="Ảnh đại diện xem trước" className="avatar-preview" fallback="/poster-fallback.svg" /><span><Image /></span></div><div><strong>{profile.data.username}</strong><span>{profile.data.email}</span><small>Không có API upload file; hãy nhập URL công khai.</small></div></div><div className="form-grid"><label>Họ và tên<input {...register('fullName')} />{errors.fullName && <small className="field-error">{errors.fullName.message}</small>}</label><label>Số điện thoại<input {...register('phone')} />{errors.phone && <small className="field-error">{errors.phone.message}</small>}</label><label className="full-span">URL avatar<input type="url" placeholder="https://..." {...register('avatar')} />{errors.avatar && <small className="field-error">{errors.avatar.message}</small>}</label></div><button className="button button-primary" disabled={update.isPending}>{update.isPending ? <><LoadingSpinner /> Đang lưu…</> : <><Save /> Lưu thay đổi</>}</button></form></div>
}
