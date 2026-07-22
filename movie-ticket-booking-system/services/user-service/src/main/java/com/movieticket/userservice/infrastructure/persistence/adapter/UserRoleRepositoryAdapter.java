package com.movieticket.userservice.infrastructure.persistence.adapter;

import com.movieticket.userservice.domain.entity.UserRole;
import com.movieticket.userservice.domain.repository.UserRoleRepository;
import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.mapper.UserRoleMapper;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryAdapter
        implements UserRoleRepository {


    private final JpaUserRoleRepository jpaUserRoleRepository;



    @Override
    public UserRole save(
            UserRole userRole
    ) {

        UserRoleJpaEntity entity =
                UserRoleMapper.toJpa(userRole);


        UserRoleJpaEntity savedEntity =
                jpaUserRoleRepository.save(entity);


        return UserRoleMapper.toDomain(savedEntity);
    }



    @Override
    public List<UserRole> findByUserId(
            Long userId
    ) {

        return jpaUserRoleRepository
                .findByUserId(userId)

                .stream()

                .map(UserRoleMapper::toDomain)

                .toList();
    }



    @Override
    public List<UserRole> findByRoleId(
            Long roleId
    ) {

        return jpaUserRoleRepository
                .findByRoleId(roleId)

                .stream()

                .map(UserRoleMapper::toDomain)

                .toList();
    }



    @Override
    public boolean existsByUserIdAndRoleId(
            Long userId,
            Long roleId
    ) {

        return jpaUserRoleRepository
                .existsByUserIdAndRoleId(
                        userId,
                        roleId
                );
    }



    @Override
    public List<String> findRoleNamesByUserId(
            Long userId
    ) {

        return jpaUserRoleRepository
                .findRoleNamesByUserId(
                        userId
                );
    }



    @Override
    public void deleteByUserId(
            Long userId
    ) {

        jpaUserRoleRepository
                .deleteByUserId(userId);
    }



    @Override
    public void deleteByUserIdAndRoleId(
            Long userId,
            Long roleId
    ) {

        jpaUserRoleRepository
                .deleteByUserIdAndRoleId(
                        userId,
                        roleId
                );
    }

    @Override
    public void updateRoleId(
            Long oldRoleId,
            Long newRoleId
    ) {

        jpaUserRoleRepository.updateRoleId(
                oldRoleId,
                newRoleId
        );

    }
}