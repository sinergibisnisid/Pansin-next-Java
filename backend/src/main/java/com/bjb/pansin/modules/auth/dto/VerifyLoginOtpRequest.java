package com.bjb.pansin.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyLoginOtpRequest {
    @NotBlank
    private String otpSessionId;
    
    @NotBlank
    private String otp;
}
