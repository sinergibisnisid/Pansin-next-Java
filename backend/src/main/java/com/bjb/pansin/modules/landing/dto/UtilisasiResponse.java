package com.bjb.pansin.modules.landing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisasiResponse {
    private long totalVaults;
    private long vaultsOpen;
    private long vaultsClosed;
    private double utilisasiPercentage;
    private long alarmsLast24Hours;
    private long totalDevices;
    private long devicesOnline;
    private long devicesOffline;
}
