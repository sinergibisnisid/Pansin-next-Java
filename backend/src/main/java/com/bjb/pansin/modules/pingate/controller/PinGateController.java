package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.activity.service.ActivityLogService;
import com.bjb.pansin.modules.pingate.dto.PinGateRequest;
import com.bjb.pansin.modules.pingate.service.TotpService;
import com.bjb.pansin.modules.pingate.service.StaticPinService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@Tag(name = "Public - PIN Gate")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/public")
@RequiredArgsConstructor
public class PinGateController {

    private final TotpService totpService;
    private final StaticPinService staticPinService;
    private final ActivityLogService activityLogService;

    @PostMapping("/verify-pin")
    public ResponseEntity<ApiResponse<String>> verifyPin(@Valid @RequestBody PinGateRequest req,
                                                          HttpServletRequest http) {
        String clientIp = getClientIp(http);
        // Use TOTP with RFC 6238 implementation (fixed)
        boolean valid = totpService.verify(req.getPin(), clientIp);

        if (valid) {
            activityLogService.log("PIN_GATE_SUCCESS", "PIN gate access granted",
                    clientIp, http.getHeader("User-Agent"), null);
            String sessionToken = UUID.randomUUID().toString();
            return ResponseEntity.ok(ApiResponse.ok("Access granted", sessionToken));
        } else {
            activityLogService.log("PIN_GATE_FAILED", "PIN gate access denied",
                    clientIp, http.getHeader("User-Agent"), null);
            log.warn("PIN gate failed attempt from IP {}", clientIp);
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid PIN"));
        }
    }

    private String getClientIp(HttpServletRequest http) {
        String fwd = http.getHeader("X-Forwarded-For");
        return fwd != null && !fwd.isBlank() ? fwd.split(",")[0].trim() : http.getRemoteAddr();
    }
}
