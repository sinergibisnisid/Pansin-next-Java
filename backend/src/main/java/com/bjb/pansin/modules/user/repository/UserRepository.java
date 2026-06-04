package com.bjb.pansin.modules.user.repository;

import com.bjb.pansin.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (LOWER(u.username) = LOWER(:value) OR LOWER(u.email) = LOWER(:value))
              AND u.deletedAt IS NULL
            """)
    Optional<User> findActiveByUsernameOrEmail(@Param("value") String value);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
