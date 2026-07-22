-- ============================================================
-- BOOKING SERVICE DATABASE & SEED DATA SCRIPT
-- Database Engine: PostgreSQL 14+
-- Target Database: booking_db
-- ============================================================

-- 1. TẠO DATABASE (Nếu chưa tồn tại)
SELECT 'CREATE DATABASE booking_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'booking_db')\gexec

-- Kết nối tới database booking_db
\c booking_db;

-- 2. TẠO BẢNG (DDL chuẩn hóa khớp với JPA Entities của Booking Service)

-- Bảng 1: Cấu hình hệ thống đặt vé
CREATE TABLE IF NOT EXISTS booking_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 2: Giữ ghế tạm thời
CREATE TABLE IF NOT EXISTS seat_holds (
    id BIGSERIAL PRIMARY KEY,
    hold_token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 3: Chi tiết các ghế được giữ tạm thời
CREATE TABLE IF NOT EXISTS seat_hold_seats (
    id BIGSERIAL PRIMARY KEY,
    hold_id BIGINT NOT NULL REFERENCES seat_holds(id) ON DELETE CASCADE,
    showtime_id BIGINT NOT NULL,
    seat_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uc_seat_hold_showtime_seat UNIQUE (showtime_id, seat_code)
);

-- Bảng 4: Đơn đặt vé (Bookings)
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT',
    hold_token VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 5: Chi tiết ghế trong đơn đặt vé
CREATE TABLE IF NOT EXISTS booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    showtime_id BIGINT NOT NULL,
    seat_code VARCHAR(20) NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    price NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uc_booking_showtime_seat UNIQUE (showtime_id, seat_code)
);

-- Bảng 6: Vé đã phát hành
CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    showtime_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    movie_title VARCHAR(255) NOT NULL,
    cinema_id BIGINT NOT NULL,
    cinema_name VARCHAR(255) NOT NULL,
    hall_id BIGINT NOT NULL,
    hall_name VARCHAR(100) NOT NULL,
    seat_code VARCHAR(20) NOT NULL,
    seat_type VARCHAR(20) NOT NULL,
    show_date DATE NOT NULL,
    start_time VARCHAR(20) NOT NULL,
    end_time VARCHAR(20) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    qr_payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 7: Lịch sử thanh toán
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    transaction_ref VARCHAR(100) NOT NULL UNIQUE,
    method VARCHAR(20) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    failure_reason VARCHAR(255),
    paid_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 8: Quản lý giao dịch Saga
CREATE TABLE IF NOT EXISTS saga_transactions (
    id BIGSERIAL PRIMARY KEY,
    saga_id VARCHAR(100) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    current_step VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bảng 9: Outbox Event Pattern
CREATE TABLE IF NOT EXISTS booking_event_outbox (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL UNIQUE,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    booking_id BIGINT,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITHOUT TIME ZONE
);

-- Bảng 10: Xử lý Idempotency
CREATE TABLE IF NOT EXISTS idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    request_hash VARCHAR(100),
    operation_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_body TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- 3. XÓA VÀ RESTART SEQUENCE (Đảm bảo chèn dữ liệu không bị trùng khóa chính)
TRUNCATE TABLE idempotency_records, booking_event_outbox, saga_transactions, payments, tickets, booking_seats, bookings, seat_hold_seats, seat_holds, booking_settings RESTART IDENTITY CASCADE;

-- 4. INSERT DỮ LIỆU MẪU (SEED DATA)

-- 4.1. Booking Settings
INSERT INTO booking_settings (setting_key, setting_value, description)
VALUES
    ('seat_hold_ttl_minutes',          '5',     'Thời gian giữ ghế tạm thời (phút)'),
    ('max_seats_per_hold',             '8',     'Số ghế tối đa mỗi lần giữ'),
    ('lock_wait_time_seconds',         '2',     'Thời gian chờ lock Redis (giây)'),
    ('lock_lease_time_seconds',        '10',    'Thời gian lock Redis (giây)'),
    ('default_ticket_price',           '90000', 'Giá vé mặc định (VNĐ)'),
    ('hold_payment_extension_minutes', '30',    'Thời gian gia hạn hold khi chuyển sang VNPay (phút)');

-- 4.2. Seat Holds
INSERT INTO seat_holds (id, hold_token, user_id, showtime_id, status, expires_at, created_at, updated_at)
VALUES
    (1, 'HOLD_EXPIRED_001', 1, 101, 'EXPIRED',   NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '15 minutes', NOW() - INTERVAL '10 minutes'),
    (2, 'HOLD_ACTIVE_001',  2, 101, 'ACTIVE',    NOW() + INTERVAL '4 minutes',  NOW() - INTERVAL '1 minute',  NOW() - INTERVAL '1 minute');

-- 4.3. Seat Hold Seats
INSERT INTO seat_hold_seats (hold_id, showtime_id, seat_code, created_at)
VALUES
    (1, 101, 'A1', NOW() - INTERVAL '15 minutes'),
    (1, 101, 'A2', NOW() - INTERVAL '15 minutes'),
    (2, 101, 'B1', NOW() - INTERVAL '1 minute'),
    (2, 101, 'B2', NOW() - INTERVAL '1 minute');

-- 4.4. Bookings
INSERT INTO bookings (id, booking_code, user_id, showtime_id, total_amount, status, hold_token, created_at, updated_at)
VALUES
    (1, 'BK_CONFIRMED_001', 1, 101, 180000.00, 'CONFIRMED',       'HOLD_CONVERTED_001', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    (2, 'BK_PENDING_001',   2, 101,  90000.00, 'PENDING_PAYMENT', 'HOLD_ACTIVE_001',    NOW() - INTERVAL '1 minute',  NOW() - INTERVAL '1 minute'),
    (3, 'BK_CANCELLED_001', 3, 102, 120000.00, 'CANCELLED',       'HOLD_CANCELLED_001', NOW() - INTERVAL '1 day',    NOW() - INTERVAL '1 day'),
    (4, 'BK_FAILED_001',    4, 103, 150000.00, 'FAILED',          'HOLD_FAILED_001',    NOW() - INTERVAL '3 hours',  NOW() - INTERVAL '3 hours');

-- 4.5. Booking Seats
INSERT INTO booking_seats (booking_id, showtime_id, seat_code, seat_type, price, status, created_at)
VALUES
    (1, 101, 'A1', 'NORMAL',  90000.00, 'CONFIRMED', NOW() - INTERVAL '2 hours'),
    (1, 101, 'A2', 'NORMAL',  90000.00, 'CONFIRMED', NOW() - INTERVAL '2 hours'),
    (2, 101, 'B1', 'VIP',     90000.00, 'PENDING',   NOW() - INTERVAL '1 minute'),
    (3, 102, 'C1', 'NORMAL', 120000.00, 'CANCELLED', NOW() - INTERVAL '1 day'),
    (4, 103, 'D1', 'COUPLE', 150000.00, 'CANCELLED', NOW() - INTERVAL '3 hours');

-- 4.6. Tickets
INSERT INTO tickets (id, ticket_code, booking_id, user_id, showtime_id, movie_id, movie_title, cinema_id, cinema_name, hall_id, hall_name, seat_code, seat_type, show_date, start_time, end_time, price, qr_payload, status, issued_at, created_at, updated_at)
VALUES
    (1, 'TK_CONFIRMED_001', 1, 1, 101, 201, 'Avengers: Endgame', 301, 'CGV Vincom', 401, 'Hall 1',
     'A1', 'NORMAL', CURRENT_DATE, '19:00', '21:30', 90000.00,
     'QR:TK_CONFIRMED_001:AVENGERS:A1:202607011900',
     'ACTIVE', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),

    (2, 'TK_CONFIRMED_002', 1, 1, 101, 201, 'Avengers: Endgame', 301, 'CGV Vincom', 401, 'Hall 1',
     'A2', 'NORMAL', CURRENT_DATE, '19:00', '21:30', 90000.00,
     'QR:TK_CONFIRMED_002:AVENGERS:A2:202607011900',
     'ACTIVE', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours');

-- 4.7. Payments
INSERT INTO payments (id, booking_id, transaction_ref, method, amount, status, paid_at, created_at, updated_at)
VALUES
    (1, 1, 'TXN_MOCK_001',   'MOCK',  180000.00, 'PAID',    NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    (2, 2, 'TXN_VNPAY_001',  'VNPAY',  90000.00, 'PENDING', NULL,                        NOW() - INTERVAL '1 minute', NOW() - INTERVAL '1 minute'),
    (3, 3, 'TXN_REFUND_001', 'MOCK',  120000.00, 'PAID',    NOW() - INTERVAL '1 day',    NOW() - INTERVAL '1 day',    NOW() - INTERVAL '1 day'),
    (4, 4, 'TXN_FAILED_001', 'MOCK',  150000.00, 'FAILED',  NULL,                        NOW() - INTERVAL '3 hours',  NOW() - INTERVAL '3 hours');

-- 4.8. Saga Transactions
INSERT INTO saga_transactions (id, saga_id, booking_id, status, current_step, failure_reason, created_at, updated_at)
VALUES
    (1, 'SAGA_CONFIRMED_001', 1, 'COMPLETED',   'ISSUE_TICKET', NULL,                      NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
    (2, 'SAGA_PENDING_001',   2, 'STARTED',     'PAYMENT',      NULL,                      NOW() - INTERVAL '1 minute', NOW() - INTERVAL '1 minute'),
    (3, 'SAGA_CANCELLED_001', 3, 'COMPLETED',   'CANCEL',       NULL,                      NOW() - INTERVAL '1 day',    NOW() - INTERVAL '1 day'),
    (4, 'SAGA_FAILED_001',    4, 'COMPENSATED', 'PAYMENT',      'Payment gateway timeout', NOW() - INTERVAL '3 hours',  NOW() - INTERVAL '3 hours');

-- 4.9. Booking Event Outbox
INSERT INTO booking_event_outbox (event_id, aggregate_type, aggregate_id, booking_id, event_type, topic, payload_json, status, retry_count, created_at)
VALUES
    ('EVT_BOOKING_CONFIRMED_001', 'BOOKING', 'BK_CONFIRMED_001', 1, 'BookingConfirmed', 'booking.confirmed',
     '{"bookingCode":"BK_CONFIRMED_001","userId":1,"showtimeId":101}', 'PUBLISHED', 0, NOW() - INTERVAL '2 hours'),

    ('EVT_TICKET_BOOKED_001', 'TICKET', 'TK_CONFIRMED_001', 1, 'TicketBookedEvent', 'booking.ticket-booked',
     '{"bookingCode":"BK_CONFIRMED_001","tickets":[{"ticketCode":"TK_CONFIRMED_001","seatCode":"A1"},{"ticketCode":"TK_CONFIRMED_002","seatCode":"A2"}]}', 'PUBLISHED', 0, NOW() - INTERVAL '2 hours'),

    ('EVT_PENDING_001', 'BOOKING', 'BK_PENDING_001', 2, 'SeatHoldCreated', 'booking.seat-hold.created',
     '{"holdToken":"HOLD_ACTIVE_001","userId":2,"showtimeId":101,"seatCodes":["B1","B2"]}', 'PENDING', 0, NOW() - INTERVAL '1 minute');

-- 4.10. Idempotency Records
INSERT INTO idempotency_records (idempotency_key, request_hash, operation_type, status, response_body, created_at, expires_at)
VALUES
    ('IDEM_HOLD_001',    'hash_hold_001',    'HOLD',    'SUCCEEDED', '{"holdToken":"HOLD_ACTIVE_001"}',                             NOW() - INTERVAL '1 minute', NOW() + INTERVAL '4 minutes'),
    ('IDEM_CONFIRM_001', 'hash_confirm_001', 'CONFIRM', 'SUCCEEDED', '{"bookingCode":"BK_CONFIRMED_001","status":"CONFIRMED"}',   NOW() - INTERVAL '2 hours',  NOW() + INTERVAL '1 day'),
    ('IDEM_CANCEL_001',  'hash_cancel_001',  'CANCEL',  'SUCCEEDED', '{"bookingCode":"BK_CANCELLED_001","status":"CANCELLED"}',    NOW() - INTERVAL '1 day',    NOW() + INTERVAL '6 days');

-- 5. CẬP NHẬT LẠI SEQUENCE COUNTER
SELECT setval('booking_settings_id_seq', COALESCE((SELECT MAX(id) FROM booking_settings), 1));
SELECT setval('seat_holds_id_seq', COALESCE((SELECT MAX(id) FROM seat_holds), 1));
SELECT setval('seat_hold_seats_id_seq', COALESCE((SELECT MAX(id) FROM seat_hold_seats), 1));
SELECT setval('bookings_id_seq', COALESCE((SELECT MAX(id) FROM bookings), 1));
SELECT setval('booking_seats_id_seq', COALESCE((SELECT MAX(id) FROM booking_seats), 1));
SELECT setval('tickets_id_seq', COALESCE((SELECT MAX(id) FROM tickets), 1));
SELECT setval('payments_id_seq', COALESCE((SELECT MAX(id) FROM payments), 1));
SELECT setval('saga_transactions_id_seq', COALESCE((SELECT MAX(id) FROM saga_transactions), 1));
SELECT setval('booking_event_outbox_id_seq', COALESCE((SELECT MAX(id) FROM booking_event_outbox), 1));
SELECT setval('idempotency_records_id_seq', COALESCE((SELECT MAX(id) FROM idempotency_records), 1));
