package com.bjb.pansin.modules.snapshot.service;

import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.livestream.config.MediaMtxProperties;
import com.bjb.pansin.modules.livestream.service.MediaMtxClient;
import com.bjb.pansin.modules.snapshot.entity.Snapshot;
import com.bjb.pansin.modules.snapshot.repository.SnapshotRepository;
import com.bjb.pansin.modules.vault.entity.Vault;
import com.bjb.pansin.modules.vault.event.VaultOpenedEvent;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import com.bjb.pansin.modules.vault.repository.VaultSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final SnapshotRepository snapshotRepository;
    private final VaultRepository vaultRepository;
    private final VaultSessionRepository sessionRepository;
    private final DeviceRepository deviceRepository;
    private final MediaMtxClient mediaMtxClient;
    private final MediaMtxProperties props;

    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void on(VaultOpenedEvent event) {
        Vault vault = vaultRepository.findById(event.getVaultId()).orElse(null);
        if (vault == null) return;

        List<Device> cameras = deviceRepository.findByBranchId(vault.getBranch().getId()).stream()
                .filter(d -> "CAMERA".equalsIgnoreCase(d.getType()))
                .toList();

        for (Device cam : cameras) {
            try {
                String streamPath = "vault-" + vault.getCode().toLowerCase();
                byte[] image = mediaMtxClient.takeSnapshot(streamPath);
                String filePath = saveImage(image, vault.getCode(), event.getSessionId());

                snapshotRepository.save(Snapshot.builder()
                        .vault(vault)
                        .session(event.getSessionId() != null
                                ? sessionRepository.findById(event.getSessionId()).orElse(null) : null)
                        .device(cam)
                        .filePath(filePath)
                        .fileSize(image != null ? (long) image.length : null)
                        .mimeType("image/jpeg")
                        .trigger("VAULT_OPENED")
                        .build());
            } catch (Exception ex) {
                log.warn("Snapshot capture failed for vault {} cam {}: {}",
                        vault.getCode(), cam.getDeviceCode(), ex.getMessage());
            }
        }
    }

    private String saveImage(byte[] data, String vaultCode, UUID sessionId) throws IOException {
        if (data == null || data.length == 0) {
            return "(no-image)";
        }
        Path dir = Paths.get(props.getSnapshotPath() != null ? props.getSnapshotPath() : "./storage/snapshots",
                vaultCode);
        Files.createDirectories(dir);
        String fileName = "%s_%s.jpg".formatted(
                sessionId != null ? sessionId : "manual",
                Instant.now().toEpochMilli());
        Path file = dir.resolve(fileName);
        Files.write(file, data);
        return file.toString();
    }
}
