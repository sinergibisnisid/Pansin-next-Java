package com.bjb.pansin.modules.landing.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.landing.dto.UtilisasiResponse;
import com.bjb.pansin.modules.landing.service.LandingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public - Landing Page")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/public")
@RequiredArgsConstructor
public class LandingController {

    private final LandingService landingService;

    @GetMapping("/utilisasi")
    public ResponseEntity<ApiResponse<UtilisasiResponse>> getUtilisasi() {
        return ResponseEntity.ok(ApiResponse.ok(landingService.getUtilisasi()));
    }
}
