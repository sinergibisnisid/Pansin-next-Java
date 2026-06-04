package com.bjb.pansin.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean otpRequired;
    private String otpSessionId;
    private int expiresIn;
    private String message;
}
