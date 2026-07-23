package com.movieticket.bookingservice.domain.aggregate;

import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import com.movieticket.bookingservice.domain.event.BookingCancelledEvent;
import com.movieticket.bookingservice.domain.event.BookingConfirmedEvent;
import com.movieticket.bookingservice.domain.event.DomainEvent;
import com.movieticket.bookingservice.domain.event.TicketBookedEvent;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookingAggregate {

    private Booking booking;
    private List<Ticket> tickets;
    private Payment payment;
    private SeatHold seatHold;
    private SagaTransaction saga;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private BookingAggregate() {}

    public static BookingAggregate forNewConfirm(SeatHold seatHold, Booking booking,
                                                 Payment payment, SagaTransaction saga) {
        BookingAggregate agg = new BookingAggregate();
        agg.seatHold = seatHold;
        agg.booking = booking;
        agg.payment = payment;
        agg.saga = saga;
        agg.tickets = new ArrayList<>();
        return agg;
    }

    public static BookingAggregate forExistingCancel(Booking booking, List<Ticket> tickets, Payment payment, SeatHold seatHold) {
        BookingAggregate agg = new BookingAggregate();
        agg.booking = booking;
        agg.tickets = tickets != null ? tickets : new ArrayList<>();
        agg.payment = payment;
        agg.seatHold = seatHold;
        return agg;
    }

    public void confirmBooking(List<Ticket> issuedTickets) {
        confirmBooking(issuedTickets, null, null, null);
    }

    public void confirmBooking(List<Ticket> issuedTickets, String movieTitle, LocalDate showDate, LocalTime startTime) {
        if (!seatHold.isActive()) {
            booking.fail("Hold expired or inactive");
            throw new IllegalStateException("Seat hold has expired or is not active");
        }
        if (payment.getTransactionRef() == null) {
            throw new IllegalStateException("Payment must have a transaction reference before confirming booking");
        }
        payment.markPaid(payment.getTransactionRef());
        booking.confirm();
        tickets = issuedTickets;
        tickets.forEach(Ticket::issue);
        seatHold.convert();
        saga.complete();

        List<String> seatCodes = tickets.stream()
                .map(Ticket::getSeatCode)
                .toList();
        List<TicketBookedEvent.TicketInfo> ticketInfos = tickets.stream()
                .map(t -> new TicketBookedEvent.TicketInfo(t.getTicketCode(), t.getSeatCode()))
                .toList();

        domainEvents.add(new BookingConfirmedEvent(
                booking.getBookingCode(), booking.getUserId(), booking.getCustomerEmail(), booking.getCustomerName(),
                booking.getShowtimeId(), movieTitle, showDate, startTime, seatCodes,
                booking.getTotalAmount(), payment.getMethod()));
        domainEvents.add(new TicketBookedEvent(
                booking.getBookingCode(), booking.getUserId(), booking.getCustomerEmail(), booking.getCustomerName(),
                booking.getShowtimeId(), movieTitle, showDate, startTime, booking.getTotalAmount(), ticketInfos));
    }

    public void compensateFailedPayment(String failureReason) {
        String reason = failureReason != null ? failureReason : "Payment failed";
        booking.fail(reason);
        if (seatHold != null) {
            seatHold.release();
        }
        if (saga != null) {
            saga.startCompensation();
            saga.compensate();
        }

        domainEvents.add(new BookingCancelledEvent(
                booking.getBookingCode(), booking.getUserId(), reason));
    }

    public void cancelBooking(String reason) {
        booking.cancel();
        if (seatHold != null) {
            seatHold.release();
        }
        domainEvents.add(new BookingCancelledEvent(booking.getBookingCode(), booking.getUserId(), reason));
    }

    public Booking getBooking() { return booking; }
    public List<Ticket> getTickets() { return Collections.unmodifiableList(tickets); }
    public Payment getPayment() { return payment; }
    public SeatHold getSeatHold() { return seatHold; }
    public SagaTransaction getSaga() { return saga; }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
    public void clearDomainEvents() { domainEvents.clear(); }
}
