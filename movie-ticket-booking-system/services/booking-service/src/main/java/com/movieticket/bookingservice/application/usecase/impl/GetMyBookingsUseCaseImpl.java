package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.PagedResponse;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetMyBookingsUseCaseImpl {

    private final JpaBookingRepository bookingRepository;
    private final JpaTicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> findByUserId(Long userId, int page, int size) {
        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, PageRequest.of(page, size));
        long totalElements = bookingPage.getTotalElements();
        int totalPages = bookingPage.getTotalPages();

        List<BookingResponse> items = bookingPage.getContent().stream()
                .map(b -> {
                    List<Ticket> tickets = ticketRepository.findByBookingId(b.getId());
                    return BookingResponseMapper.toResponse(b, tickets);
                })
                .collect(Collectors.toList());

        return PagedResponse.<BookingResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}