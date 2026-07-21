package com.movieticket.userservice.infrastructure.persistence.adapter;



import com.movieticket.userservice.domain.entity.RefreshToken;
import com.movieticket.userservice.domain.repository.RefreshTokenRepository;
import com.movieticket.userservice.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.mapper.RefreshTokenMapper;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaRefreshTokenRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter
        implements RefreshTokenRepository {


    private final JpaRefreshTokenRepository repository;



    @Override
    public RefreshToken save(
            RefreshToken token
    ) {


        RefreshTokenJpaEntity entity =
                RefreshTokenMapper.toJpa(token);


        RefreshTokenJpaEntity saved =
                repository.save(entity);


        return RefreshTokenMapper.toDomain(saved);

    }



    @Override
    public Optional<RefreshToken> findByToken(
            String token
    ) {


        return repository.findByToken(token)
                .map(
                        RefreshTokenMapper::toDomain
                );

    }



    @Override
    public List<RefreshToken> findByUserId(
            Long userId
    ) {


        return repository.findByUserId(userId)

                .stream()

                .map(
                        RefreshTokenMapper::toDomain
                )

                .toList();

    }



    @Override
    public void deleteByUserId(
            Long userId
    ) {

        repository.deleteByUserId(userId);

    }



    @Override
    public void deleteByToken(
            String token
    ) {

        repository.deleteByToken(token);

    }

}