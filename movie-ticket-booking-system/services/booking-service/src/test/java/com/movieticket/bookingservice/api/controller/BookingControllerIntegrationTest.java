package com.movieticket.bookingservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.api.dto.ConfirmBookingRequest;
import com.movieticket.bookingservice.api.dto.HoldSeatsRequest;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.security.BookingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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
    private BookingContext bookingContext;

    @BeforeEach
    void setUp() throws Exception {
        when(bookingContext.getCurrentUserId()).thenReturn(1L);
        when(showtimeClient.getShowtime(anyLong())).thenReturn(Map.of("id", 1));
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
    }

    @Test
    void holdSeats_and_confirmBooking_MockFlow() throws Exception {
        // ========== POST /api/bookings/hold-seats ==========
        HoldSeatsRequest holdRequest = new HoldSeatsRequest(1L, List.of("A1", "A2"));
        String holdResponse = mockMvc.perform(post("/api/bookings/hold-seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(holdRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.holdToken").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String holdToken = objectMapper.readTree(holdResponse)
                .get("data").get("holdToken").asText();

        // ========== POST /api/bookings/confirm (MOCK) ==========
        ConfirmBookingRequest confirmRequest = new ConfirmBookingRequest(holdToken, "MOCK", null);
        mockMvc.perform(post("/api/bookings/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.tickets").isArray());
    }

    @Test
    void holdSeats_duplicateSeats_returnsError() throws Exception {
        HoldSeatsRequest request = new HoldSeatsRequest(1L, List.of("A1", "A1"));
        mockMvc.perform(post("/api/bookings/hold-seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminSettings_CRUD() throws Exception {
        // ========== GET all settings (empty) ==========
        mockMvc.perform(get("/api/admin/booking-settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
