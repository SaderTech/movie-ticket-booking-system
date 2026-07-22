package com.movieticket.userservice.infrastructure.persistence.adapter;

import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.domain.repository.RoleRepository;
import com.movieticket.userservice.infrastructure.persistence.mapper.RoleMapper;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpaRoleRepository;

    @Override
    public Role save(Role role) {

        return RoleMapper.toDomain(
                jpaRoleRepository.save(
                        RoleMapper.toJpa(role)
                )
        );
    }

    @Override
    public Optional<Role> findById(Long id) {

        return jpaRoleRepository.findById(id)
                .map(RoleMapper::toDomain);
    }

    @Override
    public Optional<Role> findByRoleName(String roleName) {

        return jpaRoleRepository.findByRoleName(roleName)
                .map(RoleMapper::toDomain);
    }

    @Override
    public List<Role> findAll() {

        return jpaRoleRepository.findAll()
                .stream()
                .map(RoleMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByRoleName(String roleName) {

        return jpaRoleRepository.existsByRoleName(roleName);
    }

    @Override
    public void deleteById(Long id) {

        jpaRoleRepository.deleteById(id);
    }
}