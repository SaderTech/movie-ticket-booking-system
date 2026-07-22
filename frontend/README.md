# MovieTicket Frontend

Frontend ReactJS cho hệ thống đặt vé xem phim, dùng Vite và JavaScript. Giao diện sử dụng design system dark cinematic, responsive và chỉ gọi backend qua API Gateway tại `http://localhost:8080`.

## Yêu cầu

- Node.js 18 trở lên (khuyến nghị Node.js 20 LTS).
- npm đi kèm Node.js.
- Backend, Eureka, API Gateway và các service cần thiết đã chạy trước frontend.
- API Gateway chạy tại `http://localhost:8080`.

## Cài đặt và chạy

Mở thư mục `frontend` bằng Visual Studio Code, sau đó chạy trong terminal:

```bash
npm install
```

Tạo file môi trường từ mẫu:

```powershell
Copy-Item .env.example .env
```

Trên macOS/Linux có thể dùng:

```bash
cp .env.example .env
```

Nội dung mặc định:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Khởi động frontend:

```bash
npm run dev
```

Truy cập `http://localhost:3000`. Vite được cấu hình `strictPort`, vì vậy tiến trình sẽ báo lỗi nếu port 3000 đang bị sử dụng thay vì tự đổi port.

Build production:

```bash
npm run build
```

## Xác thực và quyền quản trị

Frontend lưu session đăng nhập trong `localStorage`, tự gắn access token vào header `Authorization` và dùng refresh token khi nhận HTTP 401. Các request đồng thời dùng chung một lần refresh; nếu refresh thất bại, session bị xóa và người dùng được đưa về trang đăng nhập.

Backend không trả role trong `LoginResponse` hoặc `UserResponse`. Frontend đọc claim `roles` trong JWT ở mức client để bảo vệ giao diện. Tài khoản được xem là quản trị khi claim chứa `ADMIN` hoặc `ROLE_ADMIN`. Đây chỉ là kiểm tra giao diện; API Gateway vẫn là lớp thực thi phân quyền. Không có tài khoản admin nào được hardcode trong frontend.

Không dán hoặc in access token vào console. Khi cần chẩn đoán quyền, chỉ kiểm tra khái quát rằng JWT có claim `roles` và giá trị đúng, không chia sẻ token.

## CORS

API Gateway hiện cho phép origin `http://localhost:3000`. Nếu gặp lỗi CORS:

1. Xác nhận frontend thực sự chạy đúng port 3000.
2. Xác nhận `.env` trỏ tới `http://localhost:8080`, không trỏ trực tiếp tới port service.
3. Khởi động lại Vite sau khi sửa `.env`.
4. Kiểm tra API Gateway đã chạy và cấu hình origin vẫn là `http://localhost:3000`.

Không thêm proxy gọi trực tiếp các service 8081–8086.

## VNPay return URL

VNPay cần redirect về:

```text
http://localhost:3000/payment-result
```

Booking service dùng cấu hình:

```yaml
vnpay:
  return-url: ${VNPAY_RETURN_URL:http://localhost:3000/payment-result}
```

Khi VNPay quay lại, frontend gửi nguyên vẹn query parameters tới `GET /api/bookings/vnpay-return`. Chữ ký chỉ được xác minh ở backend.

## Giới hạn hiện tại của backend

- Không có API quên mật khẩu, đăng nhập Google hoặc upload ảnh; avatar/poster chỉ nhận URL.
- Không có API danh sách ghế đã giữ/đã bán theo suất chiếu. Sơ đồ chỉ hiển thị trạng thái tĩnh của ghế; xung đột được kiểm tra khi gọi hold API.
- Booking có thể tính giá khác `showtime.price`; frontend chỉ coi giá suất chiếu là tạm tính và dùng `totalAmount` từ Booking API làm số tiền chính thức.
- Mã nguồn booking hiện tại chỉ chấp nhận `VNPAY` khi confirm. MOCK được hiển thị vô hiệu hóa trong môi trường dev để không gửi request chắc chắn thất bại.
- Một số ticket có thể thiếu metadata phim, rạp hoặc phòng; giao diện dùng giá trị fallback và không hiển thị `undefined`.
- Không có API danh sách mọi booking cho admin, doanh thu hoặc thống kê bán vé; dashboard không tạo số liệu giả.
- Không có API quản lý role. Trang người dùng không hiển thị cột role hoặc chức năng cấp quyền.
- Không có API xóa movie, cinema, hall, seat, seat type, genre, actor hoặc director.
- Notification service hiện chỉ triển khai gửi EMAIL hoàn chỉnh. SMS có thể được backend ghi nhận ở trạng thái thất bại; giao diện hiển thị đúng trạng thái API trả về.
- Không có WebSocket cập nhật ghế thời gian thực, hoàn tiền tự động, mã giảm giá, đồ ăn hoặc điểm thành viên.
- Movie API có rate limit. Ô tìm phim debounce 500 ms và không retry HTTP 429.

## Cấu trúc chính

```text
src/
  api/          Axios client và API theo service
  components/   Component dùng chung, phim, rạp, booking
  config/       Tên thương hiệu, URL và route
  contexts/     AuthContext
  hooks/        useAuth, useCountdown
  layouts/      Public, account và admin layout
  pages/        Public, auth, account, booking, admin, error
  routes/       Route guard và AppRoutes
  utils/        Auth storage, JWT, lỗi API, định dạng, YouTube
```

## Giao diện và chuyển động

- Màu thương hiệu, tên và tagline được cấu hình tập trung trong `src/config/appConfig.js`; CSS token nằm trong `src/styles/variables.css`.
- CSS được tách theo reset, utility, animation, component, layout, page, admin, responsive và print.
- Framer Motion cung cấp page transition, scroll reveal, stagger card và overlay transition. Toàn bộ chuyển động tôn trọng `prefers-reduced-motion`.
- Route được lazy-load; Vite tách riêng React, data, form và motion vendor chunk.
- Modal và drawer hỗ trợ Escape, khóa body scroll và focus trap cơ bản.
- Vé điện tử có stylesheet in qua `window.print()`; không tạo chức năng PDF giả.
