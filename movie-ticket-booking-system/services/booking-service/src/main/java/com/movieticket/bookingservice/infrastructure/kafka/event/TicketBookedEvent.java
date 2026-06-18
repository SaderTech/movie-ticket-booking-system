package com.movieticket.bookingservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketBookedEvent {
    private String bookingCode;
    private Long userId;
    private List<String> ticketCodes;
    private List<String> seatCodes;
}
