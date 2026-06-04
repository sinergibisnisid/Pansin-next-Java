package com.bjb.pansin.modules.role.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.modules.role.dto.RoleRequest;
import com.bjb.pansin.modules.role.dto.RoleResponse;
import com.bjb.pansin.modules.role.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Roles")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(roleService.list()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Role created", roleService.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody RoleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Role updated", roleService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        roleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Role deleted", null));
    }
}
