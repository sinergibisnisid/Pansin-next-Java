package com.bjb.pansin.modules.vault.service;

import com.bjb.pansin.common.enums.SessionStatus;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.entity.Vault;
import com.bjb.pansin.modules.vault.entity.VaultAccessLog;
import com.bjb.pansin.modules.vault.entity.VaultSession;
import com.bjb.pansin.modules.vault.event.VaultClosedEvent;
import com.bjb.pansin.modules.vault.event.VaultOpenedEvent;
import com.bjb.pansin.modules.vault.repository.VaultAccessLogRepository;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import com.bjb.pansin.modules.vault.repository.VaultSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultSessionService {

    private final VaultRepository vaultRepository;
    private final VaultSessionRepository sessionRepository;
    private final VaultAccessLogRepository accessLogRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.vault.session.max-duration-seconds}")
    private long maxDuration;

    @Transactional
    public VaultSession openVault(UUID vaultId, UUID userId, String method, String sourceIp) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault", vaultId));

        if (vault.getStatus() == VaultStatus.OPEN) {
            throw new BusinessException("VAULT_ALREADY_OPEN", "Vault is already open");
        }
        if (vault.getStatus() == VaultStatus.MAINTENANCE) {
            throw new BusinessException("VAULT_MAINTENANCE", "Vault is under maintenance");
        }

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        VaultSession session = VaultSession.builder()
                .vault(vault)
                .user(user)
                .openedAt(Instant.now())
                .status(SessionStatus.ACTIVE)
                .openMethod(method)
                .build();
        session = sessionRepository.save(session);

        vault.setStatus(VaultStatus.OPEN);
        vault.setLastOpenedAt(Instant.now());
        vaultRepository.save(vault);

        accessLogRepository.save(VaultAccessLog.builder()
                .vault(vault).user(user).session(session)
                .action("OPEN").success(true).method(method).sourceIp(sourceIp)
                .build());

        eventPublisher.publishEvent(new VaultOpenedEvent(
                vault.getId(), session.getId(),
                user != null ? user.getId() : null,
                method, Instant.now()));

        log.info("Vault {} opened by {} via {}", vault.getCode(),
                user != null ? user.getUsername() : "unknown", method);

        return session;
    }

    @Transactional
    public VaultSession closeVault(UUID vaultId, UUID userId, String method, String sourceIp) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new ResourceNotFoundException("Vault", vaultId));

        VaultSession session = sessionRepository
                .findFirstByVaultIdAndStatusOrderByOpenedAtDesc(vaultId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("NO_ACTIVE_SESSION", "No active session found"));

        Instant now = Instant.now();
        long duration = Duration.between(session.getOpenedAt(), now).getSeconds();
        boolean exceeded = duration > maxDuration;

        session.setClosedAt(now);
        session.setDurationSeconds((int) duration);
        session.setStatus(exceeded ? SessionStatus.TIMEOUT : SessionStatus.CLOSED);
        session.setCloseMethod(method);
        sessionRepository.save(session);

        vault.setStatus(VaultStatus.CLOSED);
        vault.setLastClosedAt(now);
        vaultRepository.save(vault);

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        accessLogRepository.save(VaultAccessLog.builder()
                .vault(vault).user(user).session(session)
                .action("CLOSE").success(true).method(method).sourceIp(sourceIp)
                .build());

        eventPublisher.publishEvent(new VaultClosedEvent(
                vault.getId(), session.getId(),
                user != null ? user.getId() : null,
                duration, exceeded, now));

        log.info("Vault {} closed (duration={}s, exceeded={})", vault.getCode(), duration, exceeded);
        return session;
    }
}
