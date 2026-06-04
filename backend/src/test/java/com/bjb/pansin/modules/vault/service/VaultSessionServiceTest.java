package com.bjb.pansin.modules.vault.service;

import com.bjb.pansin.common.enums.SessionStatus;
import com.bjb.pansin.common.enums.VaultStatus;
import com.bjb.pansin.common.exceptions.BusinessException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultSessionServiceTest {

    @Mock VaultRepository vaultRepository;
    @Mock VaultSessionRepository sessionRepository;
    @Mock VaultAccessLogRepository accessLogRepository;
    @Mock UserRepository userRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks VaultSessionService service;

    private Vault vault;
    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxDuration", 600L);

        vault = new Vault();
        vault.setId(UUID.randomUUID());
        vault.setCode("VLT-001");
        vault.setStatus(VaultStatus.CLOSED);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("alice");
    }

    @Test
    void openVaultPersistsSessionAndPublishesEvent() {
        when(vaultRepository.findById(vault.getId())).thenReturn(Optional.of(vault));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(sessionRepository.save(any(VaultSession.class)))
                .thenAnswer(inv -> {
                    VaultSession s = inv.getArgument(0);
                    s.setId(UUID.randomUUID());
                    return s;
                });
        when(vaultRepository.save(any(Vault.class))).thenAnswer(inv -> inv.getArgument(0));

        VaultSession session = service.openVault(vault.getId(), user.getId(), "MANUAL", "10.0.0.1");

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.OPEN);

        verify(accessLogRepository).save(any(VaultAccessLog.class));
        verify(eventPublisher).publishEvent(any(VaultOpenedEvent.class));
    }

    @Test
    void openVaultRejectsWhenAlreadyOpen() {
        vault.setStatus(VaultStatus.OPEN);
        when(vaultRepository.findById(vault.getId())).thenReturn(Optional.of(vault));

        assertThatThrownBy(() -> service.openVault(vault.getId(), null, "MANUAL", null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already open");
    }

    @Test
    void closeVaultRaisesAlarmEventWhenExceedingDuration() {
        VaultSession active = VaultSession.builder()
                .vault(vault).user(user)
                .openedAt(Instant.now().minusSeconds(700))   // > 600s
                .status(SessionStatus.ACTIVE)
                .build();
        active.setId(UUID.randomUUID());

        when(vaultRepository.findById(vault.getId())).thenReturn(Optional.of(vault));
        when(sessionRepository.findFirstByVaultIdAndStatusOrderByOpenedAtDesc(
                vault.getId(), SessionStatus.ACTIVE)).thenReturn(Optional.of(active));
        when(sessionRepository.save(any(VaultSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(vaultRepository.save(any(Vault.class))).thenAnswer(inv -> inv.getArgument(0));

        VaultSession closed = service.closeVault(vault.getId(), null, "MANUAL", null);

        assertThat(closed.getStatus()).isEqualTo(SessionStatus.TIMEOUT);
        assertThat(closed.getDurationSeconds()).isGreaterThan(600);
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.CLOSED);

        ArgumentCaptor<VaultClosedEvent> captor = ArgumentCaptor.forClass(VaultClosedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().isExceededLimit()).isTrue();
    }
}
