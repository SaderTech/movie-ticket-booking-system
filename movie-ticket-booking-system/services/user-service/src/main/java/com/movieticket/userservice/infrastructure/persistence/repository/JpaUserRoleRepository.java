package com.movieticket.userservice.infrastructure.persistence.repository;

import com.movieticket.userservice.infrastructure.persistence.entity.UserRoleJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaUserRoleRepository
        extends JpaRepository<
        UserRoleJpaEntity,
        UserRoleJpaEntity.UserRoleId> {

    List<UserRoleJpaEntity> findByUserId(Long userId);

    @Query("""
            select role.roleName
            from UserRoleJpaEntity userRole, RoleJpaEntity role
            where userRole.userId = :userId and userRole.roleId = role.id
            """)
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
