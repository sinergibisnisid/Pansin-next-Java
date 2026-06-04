package com.bjb.pansin.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Size(min = 3, max = 150)
    private String identifier;   // username or email

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;

    private String deviceId;
    private String userAgent;
}
