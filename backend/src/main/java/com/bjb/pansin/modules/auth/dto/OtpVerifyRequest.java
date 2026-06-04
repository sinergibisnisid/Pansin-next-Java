package com.bjb.pansin.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerifyRequest {

    @NotBlank
    private String identifier;

    @NotBlank
    @Size(min = 4, max = 8)
    private String otp;
}
