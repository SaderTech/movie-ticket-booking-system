package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
import com.movieticket.bookingservice.infrastructure.jpa.TicketJpaEntity;
import com.movieticket.bookingservice.infrastructure.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {

    private final JpaTicketRepository jpaTicketRepository;

    @Override
    public Ticket save(Ticket ticket) {
        TicketJpaEntity jpaEntity = TicketMapper.toEntity(ticket);
        TicketJpaEntity savedEntity = jpaTicketRepository.save(jpaEntity);
        return TicketMapper.toDomain(savedEntity);
    }

    @Override
    public List<Ticket> saveAll(List<Ticket> tickets) {
        List<TicketJpaEntity> jpaEntities = tickets.stream()
                .map(TicketMapper::toEntity)
                .collect(Collectors.toList());
        List<TicketJpaEntity> savedEntities = jpaTicketRepository.saveAll(jpaEntities);
        return savedEntities.stream()
                .map(TicketMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return jpaTicketRepository.findById(id)
                .map(TicketMapper::toDomain);
    }

    @Override
    public Optional<Ticket> findByTicketCode(String ticketCode) {
        return jpaTicketRepository.findByTicketCode(ticketCode)
                .map(TicketMapper::toDomain);
    }

    @Override
    public List<Ticket> findByBookingId(Long bookingId) {
        return jpaTicketRepository.findByBookingId(bookingId).stream()
                .map(TicketMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByUserId(Long userId) {
        return jpaTicketRepository.findByUserId(userId).stream()
                .map(TicketMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses) {
        return jpaTicketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(showtimeId, seatCode, statuses);
    }
}
