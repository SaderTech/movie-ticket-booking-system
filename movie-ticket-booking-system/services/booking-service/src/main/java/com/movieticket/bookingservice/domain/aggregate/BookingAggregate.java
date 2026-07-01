package com.movieticket.bookingservice.domain.aggregate;

import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import com.movieticket.bookingservice.domain.event.BookingCancelledEvent;
import com.movieticket.bookingservice.domain.event.BookingConfirmedEvent;
import com.movieticket.bookingservice.domain.event.DomainEvent;
import com.movieticket.bookingservice.domain.event.TicketBookedEvent;

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

    public static BookingAggregate forExistingCancel(Booking booking, List<Ticket> tickets, Payment payment) {
        BookingAggregate agg = new BookingAggregate();
        agg.booking = booking;
        agg.tickets = tickets != null ? tickets : new ArrayList<>();
        agg.payment = payment;
        return agg;
    }

    public void confirmBooking(List<Ticket> issuedTickets) {
        if (!seatHold.isActive()) {
            booking.fail("Hold expired or inactive");
            throw new IllegalStateException("Seat hold has expired or is not active");
        }
        payment.markPaid(payment.getTransactionRef() != null ? payment.getTransactionRef() : "TXN_" + System.currentTimeMillis());
        booking.confirm();
        tickets = issuedTickets;
        tickets.forEach(Ticket::issue);
        seatHold.convert();
        saga.complete();

        domainEvents.add(new BookingConfirmedEvent(
                booking.getBookingCode(), booking.getUserId(), booking.getShowtimeId()));
        domainEvents.add(new TicketBookedEvent(
                booking.getBookingCode(), booking.getUserId(), booking.getShowtimeId(),
                booking.getTotalAmount(),
                tickets.stream()
                        .map(t -> new TicketBookedEvent.TicketInfo(t.getTicketCode(), t.getSeatCode()))
                        .toList()));
    }

    public void compensateFailedPayment() {
        booking.fail("Payment failed");
        seatHold.release();
        saga.fail("Payment failed");

        domainEvents.add(new BookingCancelledEvent(
                booking.getBookingCode(), "Payment failed: " + (payment.getFailureReason() != null ? payment.getFailureReason() : "Unknown")));
    }

    public void cancelBooking(String reason) {
        booking.cancel();
        if (seatHold != null) {
            seatHold.release();
        }
        domainEvents.add(new BookingCancelledEvent(booking.getBookingCode(), reason));
    }

    public Booking getBooking() { return booking; }
    public List<Ticket> getTickets() { return Collections.unmodifiableList(tickets); }
    public Payment getPayment() { return payment; }
    public SeatHold getSeatHold() { return seatHold; }
    public SagaTransaction getSaga() { return saga; }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
    public void clearDomainEvents() { domainEvents.clear(); }
}
