package userservice.domain.repository;

import userservice.domain.aggregate.UserAggregate;

import java.util.Optional;

public interface UserRepository {

    UserAggregate save(UserAggregate aggregate);

    Optional<UserAggregate> findById(Long id);

    Optional<UserAggregate> findByEmail(String email);

    Optional<UserAggregate> findByUsername(String username);

    void delete(Long id);
}