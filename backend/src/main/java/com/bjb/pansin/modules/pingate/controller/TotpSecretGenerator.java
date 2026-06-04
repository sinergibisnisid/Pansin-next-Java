package com.bjb.pansin.modules.pingate.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "Debug - Generate New Secret (DEV ONLY)")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/debug/pin")
@Profile("dev")
public class TotpSecretGenerator {

    @GetMapping("/generate-secret")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateNewSecret() {
        String newSecret = new DefaultSecretGenerator().generate();
        
        Map<String, String> response = new HashMap<>();
        response.put("newSecret", newSecret);
        response.put("note", "Update application.yml with: app.security.totp.secret=" + newSecret);
        
        log.info("Generated new TOTP secret: {}", newSecret);
        
        return ResponseEntity.ok(ApiResponse.ok("New secret generated", response));
    }
}
