package com.movieticket.bookingservice.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.api.dto.ConfirmBookingRequest;
import com.movieticket.bookingservice.api.dto.HoldSeatsRequest;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.movieticket.bookingservice.infrastructure.security.BookingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class BookingControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RedissonClient redissonClient;

        @MockBean
        private RLock lock;

        @MockBean
        private ShowtimeClient showtimeClient;

        @MockBean
        private CinemaClient cinemaClient;

        @MockBean
        private MovieClient movieClient;

        @MockBean
        private SeatClient seatClient;

        @MockBean
        @org.springframework.beans.factory.annotation.Qualifier("jsonKafkaTemplate")
        private KafkaTemplate<String, Object> kafkaTemplate;

        @MockBean
        private BookingContext bookingContext;

        @Value("${vnpay.hash-secret}")
        private String vnpHashSecret;

        @BeforeEach
        void setUp() throws Exception {
                when(bookingContext.getCurrentUserId()).thenReturn(1L);

                ShowtimeResponse showtimeData = new ShowtimeResponse(
                                1L, 100L, 200L, 300L,
                                LocalDate.of(2026, 7, 15), LocalTime.of(19, 0), LocalTime.of(21, 30),
                                BigDecimal.valueOf(120_000), null, "AVAILABLE");
                when(showtimeClient.getShowtime(anyLong())).thenReturn(showtimeData);

                MovieResponse movieData = new MovieResponse(
                                100L, "Test Movie", null, null, null, "http://test.com/poster.jpg", null, null, null,
                                null, null, null,
                                null, null);
                when(movieClient.getMovie(anyLong())).thenReturn(movieData);

                CinemaResponse cinemaData = new CinemaResponse(
                                200L, "Test Cinema", null, null, null, null, null, null, null, null);
                when(cinemaClient.getCinema(anyLong())).thenReturn(cinemaData);

                List<SeatResponse> seats = List.of(
                                new SeatResponse(1L, 300L, null, "NORMAL", null, "A", 1, null),
                                new SeatResponse(2L, 300L, null, "VIP", null, "A", 2, null),
                                new SeatResponse(3L, 300L, null, "NORMAL", null, "B", 1, null),
                                new SeatResponse(4L, 300L, null, "NORMAL", null, "B", 2, null),
                                new SeatResponse(5L, 300L, null, "NORMAL", null, "C", 1, null),
                                new SeatResponse(6L, 300L, null, "NORMAL", null, "D", 1, null),
                                new SeatResponse(7L, 300L, null, "NORMAL", null, "E", 1, null),
                                new SeatResponse(8L, 300L, null, "NORMAL", null, "F", 1, null),
                                new SeatResponse(9L, 300L, null, "NORMAL", null, "G", 1, null),
                                new SeatResponse(10L, 300L, null, "NORMAL", null, "H", 1, null));
                when(seatClient.getSeatsByHallId(anyLong())).thenReturn(seats);

                when(redissonClient.getLock(anyString())).thenReturn(lock);
                when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lock.isHeldByCurrentThread()).thenReturn(true);
        }

        // ========== Helpers ==========

        private String holdSeats(List<String> seatCodes) throws Exception {
                HoldSeatsRequest request = new HoldSeatsRequest(1L, seatCodes);
                String response = mockMvc.perform(post("/api/bookings/hold-seats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.holdToken").isString())
                                .andReturn().getResponse().getContentAsString();
                return objectMapper.readTree(response).get("data").get("holdToken").asText();
        }

        private JsonNode confirmVnPay(String holdToken) throws Exception {
                ConfirmBookingRequest request = new ConfirmBookingRequest(holdToken, "VNPAY",
                                "http://localhost:8085/api/bookings/vnpay-return");
                String response = mockMvc.perform(post("/api/bookings/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                                .andExpect(jsonPath("$.data.paymentUrl").isString())
                                .andReturn().getResponse().getContentAsString();
                return objectMapper.readTree(response).get("data");
        }

        private String computeVnPayHash(Map<String, String> params) {
                TreeMap<String, String> sorted = new TreeMap<>(params);
                sorted.remove("vnp_SecureHash");
                String hashData = sorted.entrySet().stream()
                                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                                .collect(Collectors.joining("&"));
                return hmacSha512(vnpHashSecret, hashData);
        }

        private static String hmacSha512(String key, String data) {
                try {
                        Mac mac = Mac.getInstance("HmacSHA512");
                        SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
                        mac.init(spec);
                        byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        for (byte b : bytes) {
                                sb.append(String.format("%02x", b));
                        }
                        return sb.toString();
                } catch (Exception e) {
                        throw new RuntimeException("HMAC error", e);
                }
        }

        // ========== Full booking flow ==========

        @Test
        void fullVnPayFlow_holdConfirmReturn() throws Exception {
                String holdToken = holdSeats(List.of("A1", "A2"));

                JsonNode confirmData = confirmVnPay(holdToken);

                String bookingCode = confirmData.get("bookingCode").asText();
                String transactionRef = confirmData.get("payment").get("transactionRef").asText();
                BigDecimal totalAmount = new BigDecimal(confirmData.get("totalAmount").asText());

                long vnpAmount = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

                Map<String, String> vnpayParams = new LinkedHashMap<>();
                vnpayParams.put("vnp_Amount", String.valueOf(vnpAmount));
                vnpayParams.put("vnp_BankCode", "NCB");
                vnpayParams.put("vnp_BankTranNo", "20260714123456");
                vnpayParams.put("vnp_CardType", "ATM");
                vnpayParams.put("vnp_OrderInfo", "Thanh toan ve xem phim - " + bookingCode);
                vnpayParams.put("vnp_PayDate", "20260714193000");
                vnpayParams.put("vnp_ResponseCode", "00");
                vnpayParams.put("vnp_TmnCode", "TU8GEVIB");
                vnpayParams.put("vnp_TransactionNo", "1234567890");
                vnpayParams.put("vnp_TransactionStatus", "00");
                vnpayParams.put("vnp_TxnRef", transactionRef);
                String hash = computeVnPayHash(vnpayParams);
                vnpayParams.put("vnp_SecureHash", hash);

                mockMvc.perform(get("/api/bookings/vnpay-return")
                                .param("vnp_Amount", vnpayParams.get("vnp_Amount"))
                                .param("vnp_BankCode", vnpayParams.get("vnp_BankCode"))
                                .param("vnp_BankTranNo", vnpayParams.get("vnp_BankTranNo"))
                                .param("vnp_CardType", vnpayParams.get("vnp_CardType"))
                                .param("vnp_OrderInfo", vnpayParams.get("vnp_OrderInfo"))
                                .param("vnp_PayDate", vnpayParams.get("vnp_PayDate"))
                                .param("vnp_ResponseCode", vnpayParams.get("vnp_ResponseCode"))
                                .param("vnp_TmnCode", vnpayParams.get("vnp_TmnCode"))
                                .param("vnp_TransactionNo", vnpayParams.get("vnp_TransactionNo"))
                                .param("vnp_TransactionStatus", vnpayParams.get("vnp_TransactionStatus"))
                                .param("vnp_TxnRef", vnpayParams.get("vnp_TxnRef"))
                                .param("vnp_SecureHash", vnpayParams.get("vnp_SecureHash")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                                .andExpect(jsonPath("$.data.tickets").isArray())
                                .andExpect(jsonPath("$.data.tickets.length()").value(2));

                mockMvc.perform(get("/api/bookings/{code}", bookingCode))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.bookingCode").value(bookingCode))
                                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        void cancelPendingBooking_beforePayment_returnsCancelled() throws Exception {
                String holdToken = holdSeats(List.of("H1"));
                JsonNode confirmData = confirmVnPay(holdToken);
                String bookingCode = confirmData.get("bookingCode").asText();

                mockMvc.perform(post("/api/bookings/{code}/cancel", bookingCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Changed mind\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

                mockMvc.perform(get("/api/bookings/{code}", bookingCode))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        // ========== Confirm validation ==========

        @Test
        void confirm_nonVnPay_rejected() throws Exception {
                String holdToken = holdSeats(List.of("B1", "B2"));

                mockMvc.perform(post("/api/bookings/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                                new ConfirmBookingRequest(holdToken, "MOCK", null))))
                                .andExpect(status().isBadRequest());
        }

        // ========== Cancel ==========

        @Test
        void cancelBooking_notFound_returns400() throws Exception {
                mockMvc.perform(post("/api/bookings/NONEXISTENT/cancel")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"test\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void cancelBooking_forbidden_whenNotOwner() throws Exception {
                String holdToken = holdSeats(List.of("C1"));
                JsonNode confirmData = confirmVnPay(holdToken);
                String bookingCode = confirmData.get("bookingCode").asText();

                when(bookingContext.getCurrentUserId()).thenReturn(999L);

                mockMvc.perform(post("/api/bookings/{code}/cancel", bookingCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reason\":\"Not owner\"}"))
                                .andExpect(status().isForbidden());
        }

        // ========== Get booking ==========

        @Test
        void getBooking_notFound_returns400() throws Exception {
                mockMvc.perform(get("/api/bookings/NONEXISTENT"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void getBooking_forbidden_whenNotOwner() throws Exception {
                String holdToken = holdSeats(List.of("D1"));
                JsonNode confirmData = confirmVnPay(holdToken);
                String bookingCode = confirmData.get("bookingCode").asText();

                when(bookingContext.getCurrentUserId()).thenReturn(999L);

                mockMvc.perform(get("/api/bookings/{code}", bookingCode))
                                .andExpect(status().isForbidden());
        }

        // ========== My Bookings pagination ==========

        @Test
        void myBookings_afterConfirm_returnsPagedResponse() throws Exception {
                String holdToken = holdSeats(List.of("E1"));
                confirmVnPay(holdToken);

                mockMvc.perform(get("/api/bookings/my-bookings")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.items").isArray())
                                .andExpect(jsonPath("$.data.items.length()").value(1))
                                .andExpect(jsonPath("$.data.page").value(0))
                                .andExpect(jsonPath("$.data.size").value(10))
                                .andExpect(jsonPath("$.data.totalElements").value(1))
                                .andExpect(jsonPath("$.data.totalPages").value(1))
                                .andExpect(jsonPath("$.data.first").value(true))
                                .andExpect(jsonPath("$.data.last").value(true));
        }

        @Test
        void myBookings_empty_returnsEmptyPage() throws Exception {
                mockMvc.perform(get("/api/bookings/my-bookings")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.items").isEmpty())
                                .andExpect(jsonPath("$.data.totalElements").value(0))
                                .andExpect(jsonPath("$.data.totalPages").value(0))
                                .andExpect(jsonPath("$.data.first").value(true))
                                .andExpect(jsonPath("$.data.last").value(true));
        }

        // ========== Hold seats validation ==========

        @Test
        void holdSeats_duplicateSeats_returnsError() throws Exception {
                HoldSeatsRequest request = new HoldSeatsRequest(1L, List.of("A1", "A1"));
                mockMvc.perform(post("/api/bookings/hold-seats")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        // ========== VNPay return failures ==========

        @Test
        void vnpayReturn_invalidHash_returnsError() throws Exception {
                String holdToken = holdSeats(List.of("F1"));
                JsonNode confirmData = confirmVnPay(holdToken);
                String transactionRef = confirmData.get("payment").get("transactionRef").asText();

                mockMvc.perform(get("/api/bookings/vnpay-return")
                                .param("vnp_TxnRef", transactionRef)
                                .param("vnp_ResponseCode", "00")
                                .param("vnp_SecureHash", "INVALID_HASH"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void vnpayReturn_failedResponseCode_returnsFailedBooking() throws Exception {
                String holdToken = holdSeats(List.of("G1"));
                JsonNode confirmData = confirmVnPay(holdToken);
                String transactionRef = confirmData.get("payment").get("transactionRef").asText();
                BigDecimal totalAmount = new BigDecimal(confirmData.get("totalAmount").asText());
                long vnpAmount = totalAmount.multiply(BigDecimal.valueOf(100)).longValue();

                Map<String, String> vnpayParams = new LinkedHashMap<>();
                vnpayParams.put("vnp_Amount", String.valueOf(vnpAmount));
                vnpayParams.put("vnp_BankCode", "NCB");
                vnpayParams.put("vnp_BankTranNo", "FAIL20260714");
                vnpayParams.put("vnp_CardType", "ATM");
                vnpayParams.put("vnp_OrderInfo", "Thanh toan ve xem phim");
                vnpayParams.put("vnp_PayDate", "20260714193000");
                vnpayParams.put("vnp_ResponseCode", "09");
                vnpayParams.put("vnp_TmnCode", "TU8GEVIB");
                vnpayParams.put("vnp_TransactionNo", "FAIL09000001");
                vnpayParams.put("vnp_TransactionStatus", "02");
                vnpayParams.put("vnp_TxnRef", transactionRef);
                String hash = computeVnPayHash(vnpayParams);
                vnpayParams.put("vnp_SecureHash", hash);

                for (Map.Entry<String, String> e : vnpayParams.entrySet()) {
                        if (e.getKey().equals("vnp_SecureHash"))
                                continue;
                }

                mockMvc.perform(get("/api/bookings/vnpay-return")
                                .param("vnp_Amount", vnpayParams.get("vnp_Amount"))
                                .param("vnp_BankCode", vnpayParams.get("vnp_BankCode"))
                                .param("vnp_BankTranNo", vnpayParams.get("vnp_BankTranNo"))
                                .param("vnp_CardType", vnpayParams.get("vnp_CardType"))
                                .param("vnp_OrderInfo", vnpayParams.get("vnp_OrderInfo"))
                                .param("vnp_PayDate", vnpayParams.get("vnp_PayDate"))
                                .param("vnp_ResponseCode", vnpayParams.get("vnp_ResponseCode"))
                                .param("vnp_TmnCode", vnpayParams.get("vnp_TmnCode"))
                                .param("vnp_TransactionNo", vnpayParams.get("vnp_TransactionNo"))
                                .param("vnp_TransactionStatus", vnpayParams.get("vnp_TransactionStatus"))
                                .param("vnp_TxnRef", vnpayParams.get("vnp_TxnRef"))
                                .param("vnp_SecureHash", vnpayParams.get("vnp_SecureHash")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.status").value("FAILED"));
        }
}
