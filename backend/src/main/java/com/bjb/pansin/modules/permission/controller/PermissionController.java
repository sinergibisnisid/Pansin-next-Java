package com.bjb.pansin.modules.permission.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.permission.entity.Permission;
import com.bjb.pansin.modules.permission.repository.PermissionRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Permissions")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERMISSION_READ')")
    public ResponseEntity<ApiResponse<List<Permission>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(permissionRepository.findAll()));
    }
}
