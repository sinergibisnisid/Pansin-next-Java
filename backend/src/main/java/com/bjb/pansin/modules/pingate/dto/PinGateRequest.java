package com.bjb.pansin.modules.pingate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PinGateRequest {
    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "PIN must be 6 digits")
    private String pin;
}
