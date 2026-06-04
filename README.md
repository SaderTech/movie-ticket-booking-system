```markdown
# 🎬 Movie Ticket Booking System

Hệ thống đặt vé xem phim trực tuyến được xây dựng theo kiến trúc **Microservices**, tuân thủ nghiêm ngặt **Clean Architecture** và **Domain-Driven Design (DDD)**.

Dự án áp dụng mô hình quản lý mã nguồn Monorepo (Multi-module) quản lý tập trung.

---

## 🏗️ Architecture & Project Structure

Để tránh xô lệch cấu trúc thư mục trên Git, sơ đồ cây được dựng chuẩn trong thẻ code block:

```text
movie-ticket-booking-system/
├── pom.xml                              # Parent POM (Spring Boot + Spring Cloud + Lombok)
├── docker-compose.yml                   # Cấu hình hạ tầng Data (PostgreSQL, Redis, Kafka, Zookeeper)
├── .gitignore                           # Block các thư mục build (target), IDE settings
├── infra/
│   ├── api-gateway/                     # Cổng vào hệ thống, định tuyến, chống CORS (port 8080)
│   ├── config-server/                   # Quản lý file cấu hình tập trung (port 8888)
│   ├── eureka-service/                  # Service Discovery - Quầy lễ tân (port 8761)
│   └── init-scripts/                    # Script SQL (init.sql) khởi tạo 5 DB tự động cho Docker
├── services/
│   ├── booking-service/                 # Đặt vé, chọn ghế, thanh toán (port 8085)
│   ├── cinema-service/                  # Quản lý cụm rạp, phòng chiếu, ghế vật lý (port 8083)
│   ├── movie-service/                   # Quản lý kho phim, diễn viên, thể loại (port 8082)
│   ├── notification-service/            # Gửi Email/SMS mã QR vé xem phim (port 8086)
│   ├── showtime-service/                # Quản lý lịch chiếu, suất chiếu (port 8084)
│   └── user-service/                    # Quản lý tài khoản, khách hàng, nhân viên (port 8081)
└── shared/
    ├── common-events/                   # Chứa các Object/Record cho Kafka Message
    ├── common-security/                 # Logic xác thực JWT, cấu hình Spring Security dùng chung
    └── common-web/                      # Chứa Global Exception, ApiResponse, Utility Classes

```

---

## 🧩 Cấu trúc Package bên trong mỗi Service (Clean Architecture)

Mọi Service nghiệp vụ nội bộ đều tuân thủ nghiêm ngặt 4 lớp phân tách ranh giới rõ ràng:

* **`presentation/`**: Chứa REST Controller, DTO (Tiếp nhận và phản hồi Request từ API Gateway).
* **`application/`**: Chứa các Use Case, Service Interface & Implementation (Điều phối luồng nghiệp vụ).
* **`domain/`**: Chứa Core Model, Repository Interface, Custom Exceptions. *Đặc biệt: Không chứa framework annotation liên quan đến DB.*
* **`infrastructure/`**: Chứa JPA Entity, Repository Implementation, Database Configuration, Kafka Producers/Consumers, Redis Setup.

---

## 🏛️ Domain-Driven Design (DDD) Specifications

Hệ thống được phân rã thành các miền nghiệp vụ với các Aggregate Root, Entity và Value Object (VO) chuyên biệt:

### 1. User Service

* **Aggregate Root**: `User`
* **Value Objects**: `UserId`, `Email`, `Password` (Hashed), `FullName`, `Role` (Enum).

### 2. Movie Service

* **Aggregate Root**: `Movie`
* **Entities**: `Review`
* **Value Objects**: `MovieId`, `Duration`, `AgeRestriction` (Enum), `Director`, `Cast`.

### 3. Cinema Service

* **Aggregate Root**: `Cinema`
* **Entities**: `Hall` (Phòng chiếu), `Seat` (Ghế ngồi vật lý).
* **Value Objects**: `CinemaId`, `Address`, `SeatPosition`, `SeatType`, `HallType`.

### 4. Showtime Service

* **Aggregate Root**: `Showtime`
* **Value Objects**: `ShowtimeId`, `TimeSlot` (startTime, endTime), `BasePrice` (Money), Tham chiếu ID ngoại lai (`MovieId`, `CinemaId`, `HallId`).

### 5. Booking Service

* **Aggregate Root**: `Booking` (Hóa đơn tổng)
* **Entities**: `Ticket` (Chi tiết vé).
* **Value Objects**: `BookingId`, `Money`, `BookingStatus`, `SeatHold` (TTL Redis).

### 6. Notification Service

* **Aggregate Root**: `Notification`
* **Value Objects**: `NotificationId`, `Recipient`, `MessageTemplate`, `NotificationType`, `NotificationStatus`.

> ⚠️ **Lưu ý nghiệp vụ**: Các Entity và VO ở tầng Domain bắt buộc sử dụng POJO thuần túy hoặc Java `record`, không sử dụng các annotation `@Entity` hoặc `@Table` của JPA để đảm bảo tính độc lập.

---

## 🔌 Services & Ports

| Service | Port | Base Path | Trách nhiệm (Responsibility) |
| --- | --- | --- | --- |
| **`eureka-service`** | 8761 | `/` | Đăng ký và khám phá dịch vụ (Service Discovery) |
| **`config-server`** | 8888 | `/` | Quản lý file cấu hình (.yml) tập trung cho toàn hệ thống |
| **`api-gateway`** | 8080 | `/` | Định tuyến (Routing), chặn CORS, Rate-limit hệ thống |
| **`user-service`** | 8081 | `/api/users` | Quản lý hồ sơ người dùng, xác thực (Auth), phân quyền (Role) |
| **`movie-service`** | 8082 | `/api/movies` | Quản lý thông tin phim, thể loại, thông tin trailer |
| **`cinema-service`** | 8083 | `/api/cinemas` | Quản lý hạ tầng cụm rạp, phòng chiếu và sơ đồ ghế |
| **`showtime-service`** | 8084 | `/api/showtimes` | Xếp lịch chiếu, đồng bộ giá vé theo khung giờ và ngày lễ |
| **`booking-service`** | 8085 | `/api/bookings` | Đặt vé, giữ ghế trống thời gian thực, xử lý thanh toán |
| **`notification-service`** | 8086 | `/api/notifications` | Nhận Kafka event, sinh mã QR và gửi email vé cho khách hàng |

---

## 🚀 Hướng dẫn Cài đặt & Khởi chạy (Build & Run)

### Bước 1: Khởi tạo hạ tầng Data (Database, Redis, Message Queue)

Từ thư mục gốc của dự án, mở Terminal và chạy lệnh Docker để khởi động ngầm toàn bộ tài nguyên:

```bash
docker-compose up -d

```

*(Hệ thống hạ tầng tự động đọc các SQL script trong `init-scripts` để khởi tạo 5 database sạch trên PostgreSQL).*

### Bước 2: Build tổng thể dự án

Tiến hành dọn dẹp các bản build cũ và đóng gói tải các package thư viện dùng chung:

```bash
mvn clean install

```

### Bước 3: Khởi chạy các Service

Sử dụng công cụ **Run Dashboard / Services** tích hợp sẵn của IntelliJ IDEA hoặc chạy thủ công qua CLI.

⚠️ **QUY TẮC BẬT MÁY (BẮT BUỘC):**

1. Luôn khởi chạy `eureka-service` **ĐẦU TIÊN**.
2. Chờ log bảng điều khiển thông báo hệ thống đã khởi động hoàn tất (`Started...`).
3. Tiến hành khởi chạy `api-gateway`, `config-server` và các core microservices còn lại.

```bash
# Ví dụ cấu trúc lệnh chạy bằng CLI cho module cụ thể
cd services/user-service
mvn spring-boot:run

```

---

## 🧪 Smoke Test

Sau khi toàn bộ dịch vụ báo trạng thái **`UP`** trên giao diện Eureka Dashboard (`http://localhost:8761`), kiểm tra tính thông suốt của luồng định tuyến thông qua cổng Gateway chung:

```bash
# Kiểm tra phản hồi Health-check qua API Gateway (Port 8080)
curl http://localhost:8080/api/users/ping
curl http://localhost:8080/api/movies/ping
curl http://localhost:8080/api/cinemas/ping
curl http://localhost:8080/api/showtimes/ping
curl http://localhost:8080/api/bookings/ping

```

**Kết quả mong đợi (Định dạng JSON chuẩn hóa đồng nhất qua `common-web`):**

```json
{  
  "code": 200,  
  "message": "Success",  
  "data": { 
    "service": "user-service", 
    "status": "UP" 
  }  
}

```

---

## 💡 Key Challenges (Bài toán nghiệp vụ cốt lõi)

* **Concurrent Seat Booking**: Kiểm soát tranh chấp đặt vé tại các ghế hot thời gian thực (Real-time seat hold) giới hạn trong 5 phút bằng cơ chế khóa phân tán **Redis Distributed Lock (Redisson)**.
* **Data Consistency**: Đồng bộ trạng thái thông tin phi chuẩn hóa dữ liệu lưu trùng (Tên phim, lịch chiếu trong hóa đơn đặt vé) bằng kiến trúc hướng sự kiện bất đồng bộ qua **Apache Kafka**.
* **Transactional Consistency**: Đảm bảo tính nhất quán giao dịch chuỗi dịch vụ phân tán bằng việc áp dụng mô hình điều phối **Saga Pattern** hoặc **Outbox Pattern**.
* **High Availability**: Thiết kế hệ thống chịu tải cô lập tốt, đảm bảo khách hàng vẫn có thể tìm kiếm phim và lịch rạp ngay cả khi cổng đặt vé (`booking-service`) gặp sự cố gián đoạn.

---

## 🛠️ Technology Stack

* **Ngôn ngữ & Framework chính**: Java 21, Spring Boot 3.3.0, Spring Cloud 2023.0.x
* **Cơ sở dữ liệu & Bộ nhớ đệm**: PostgreSQL 15, Redis 7
* **Hệ thống trung chuyển tin nhắn**: Confluent Apache Kafka
* **Giao tiếp nội bộ**: Spring Cloud OpenFeign (Đồng bộ) & Kafka (Bất đồng bộ)
* **Thư viện bổ trợ**: Lombok, MapStruct, Jakarta Validation
* **Công cụ quản lý build**: Maven Multi-module
