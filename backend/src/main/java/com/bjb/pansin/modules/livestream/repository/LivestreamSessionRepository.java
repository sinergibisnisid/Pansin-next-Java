package com.bjb.pansin.modules.livestream.repository;

import com.bjb.pansin.modules.livestream.entity.LivestreamSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LivestreamSessionRepository extends JpaRepository<LivestreamSession, UUID> {
    Optional<LivestreamSession> findBySessionToken(String token);
}
