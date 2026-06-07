package com.bjb.pansin.modules.deviceprovisioning.dto;

import com.bjb.pansin.modules.device.entity.Device;
import com.bjb.pansin.modules.deviceprovisioning.entity.DeviceCertificate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCertificateResponse {
    private UUID id;
    private UUID deviceId;
    private String deviceCode;
    private String deviceName;
    private String serialNumber;
    private String subject;
    private String issuer;
    private String fingerprintSha256;
    private Instant validFrom;
    private Instant validUntil;
    private String status;
    private boolean revoked;
    private Instant revokedAt;
    private String revokedReason;
    private Instant issuedAt;
    private Instant createdAt;

    public static DeviceCertificateResponse from(DeviceCertificate certificate) {
        Device device = certificate.getDevice();
        return DeviceCertificateResponse.builder()
                .id(certificate.getId())
                .deviceId(device != null ? device.getId() : null)
                .deviceCode(device != null ? device.getDeviceCode() : null)
                .deviceName(device != null ? device.getName() : null)
                .serialNumber(certificate.getSerialNumber())
                .subject(certificate.getSubjectDn())
                .issuer(certificate.getIssuerDn())
                .fingerprintSha256(certificate.getFingerprintSha256())
                .validFrom(certificate.getNotBefore())
                .validUntil(certificate.getNotAfter())
                .status(certificate.getStatus())
                .revoked(certificate.getRevokedAt() != null || "REVOKED".equalsIgnoreCase(certificate.getStatus()))
                .revokedAt(certificate.getRevokedAt())
                .revokedReason(certificate.getRevokedReason())
                .issuedAt(certificate.getIssuedAt())
                .createdAt(certificate.getCreatedAt())
                .build();
    }
}
