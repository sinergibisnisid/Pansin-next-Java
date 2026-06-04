package com.bjb.pansin.modules.livestream.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.livestream.config.MediaMtxProperties;
import com.bjb.pansin.modules.livestream.dto.StartStreamRequest;
import com.bjb.pansin.modules.livestream.entity.LivestreamSession;
import com.bjb.pansin.modules.livestream.repository.LivestreamSessionRepository;
import com.bjb.pansin.modules.livestream.service.MediaMtxClient;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Tag(name = "Livestream")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/livestream")
@RequiredArgsConstructor
public class LivestreamController {

    private final LivestreamSessionRepository repository;
    private final VaultRepository vaultRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final MediaMtxClient mediaMtxClient;
    private final MediaMtxProperties props;

    @GetMapping
    @PreAuthorize("hasAuthority('LIVESTREAM_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<LivestreamSession>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repository.findAll()));
    }

    @PostMapping("/start")
    @PreAuthorize("hasAuthority('LIVESTREAM_VIEW') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<LivestreamSession>> start(@Valid @RequestBody StartStreamRequest req) {
        var vault = vaultRepository.findById(req.getVaultId())
                .orElseThrow(() -> new ResourceNotFoundException("Vault", req.getVaultId()));

        String token = UUID.randomUUID().toString().replace("-", "");
        String streamUrl = "%s/whep/%s?token=%s".formatted(
                props.getBaseUrl(), "vault-" + vault.getCode().toLowerCase(), token);

        LivestreamSession session = LivestreamSession.builder()
                .vault(vault)
                .device(req.getDeviceId() != null ? deviceRepository.findById(req.getDeviceId()).orElse(null) : null)
                .user(SecurityUtils.getCurrentUserId().flatMap(userRepository::findById).orElse(null))
                .sessionToken(token)
                .streamUrl(streamUrl)
                .startedAt(Instant.now())
                .status("ACTIVE")
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Stream started", repository.save(session)));
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAuthority('LIVESTREAM_VIEW') or hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<LivestreamSession>> stop(@PathVariable UUID id) {
        LivestreamSession session = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LivestreamSession", id));
        if ("ACTIVE".equals(session.getStatus())) {
            session.setStatus("ENDED");
            session.setEndedAt(Instant.now());
            repository.save(session);
        }
        return ResponseEntity.ok(ApiResponse.ok("Stream stopped", session));
    }

    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.ok(mediaMtxClient.ping() ? "UP" : "DOWN"));
    }
}
