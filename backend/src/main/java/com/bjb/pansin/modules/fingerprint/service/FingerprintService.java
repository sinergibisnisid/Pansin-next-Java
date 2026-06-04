package com.bjb.pansin.modules.fingerprint.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.device.repository.DeviceRepository;
import com.bjb.pansin.modules.fingerprint.entity.FingerprintLog;
import com.bjb.pansin.modules.fingerprint.repository.FingerprintLogRepository;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import com.bjb.pansin.modules.vault.service.VaultSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FingerprintService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final FingerprintLogRepository logRepository;
    private final WorkingTimeValidator workingTimeValidator;
    private final VaultSessionService vaultSessionService;

    @Transactional
    public void handleScan(UUID deviceId, UUID userId, String templateId,
                           Integer quality, Map<String, Object> rawPayload) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("DEVICE_NOT_FOUND", "Device not found"));

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        boolean matched = user != null;
        String reason = null;

        try {
            if (!matched) {
                reason = "User not found / no match";
            } else {
                ZoneId zone = device.getBranch() != null
                        ? ZoneId.of(device.getBranch().getTimezone())
                        : ZoneId.of("Asia/Jakarta");
                workingTimeValidator.validate(user.getId(), zone);

                if (!hasVaultOpenPermission(user)) {
                    throw new BusinessException("PERM_DENIED", "User lacks VAULT_OPEN permission");
                }
                if (device.getVault() != null && !belongsToSameBranch(user, device)) {
                    throw new BusinessException("BRANCH_MISMATCH",
                            "User branch does not match device branch");
                }

                if (device.getVault() != null) {
                    vaultSessionService.openVault(device.getVault().getId(),
                            user.getId(), "FINGERPRINT", null);
                }
            }
        } catch (Exception ex) {
            matched = false;
            reason = ex.getMessage();
        }

        FingerprintLog logEntry = FingerprintLog.builder()
                .device(device).user(user).templateId(templateId)
                .qualityScore(quality).matched(matched).reason(reason)
                .rawPayload(rawPayload).build();
        logRepository.save(logEntry);

        workingTimeValidator.publishScan(deviceId, user != null ? user.getId() : null,
                templateId, matched);

        log.info("Fingerprint scan device={} user={} matched={} reason={}",
                device.getDeviceCode(), user != null ? user.getUsername() : "?", matched, reason);
    }

    private boolean hasVaultOpenPermission(User user) {
        return user.getRoles().stream().anyMatch(r ->
                "SUPER_ADMIN".equals(r.getCode())
                        || r.getPermissions().stream().anyMatch(p -> "VAULT_OPEN".equals(p.getCode())));
    }

    private boolean belongsToSameBranch(User user, Device device) {
        if (user.getBranch() == null || device.getBranch() == null) return true;
        return user.getBranch().getId().equals(device.getBranch().getId());
    }
}
