package com.bjb.pansin.modules.vault.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.common.security.SecurityUtils;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.vault.dto.VaultRequest;
import com.bjb.pansin.modules.vault.entity.Vault;
import com.bjb.pansin.modules.vault.entity.VaultSession;
import com.bjb.pansin.modules.vault.repository.VaultRepository;
import com.bjb.pansin.modules.vault.service.VaultSessionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Vaults")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/vaults")
@RequiredArgsConstructor
public class VaultController {

    private final VaultRepository vaultRepository;
    private final BranchRepository branchRepository;
    private final VaultSessionService sessionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Vault>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(vaultRepository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Vault>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(vaultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vault", id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT') or hasRole('ADMIN_CABANG')")
    public ResponseEntity<ApiResponse<Vault>> create(@Valid @RequestBody VaultRequest req) {
        Vault vault = Vault.builder()
                .branch(branchRepository.findById(req.getBranchId())
                        .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId())))
                .code(req.getCode()).name(req.getName()).location(req.getLocation())
                .active(true).build();
        return ResponseEntity.ok(ApiResponse.ok("Vault created", vaultRepository.save(vault)));
    }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAuthority('VAULT_OPEN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VaultSession>> open(@PathVariable UUID id,
                                                          @RequestParam(defaultValue = "MANUAL") String method,
                                                          HttpServletRequest http) {
        UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(ApiResponse.ok("Vault opened",
                sessionService.openVault(id, userId, method, http.getRemoteAddr())));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('VAULT_CLOSE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VaultSession>> close(@PathVariable UUID id,
                                                           @RequestParam(defaultValue = "MANUAL") String method,
                                                           HttpServletRequest http) {
        UUID userId = SecurityUtils.getCurrentUserId().orElse(null);
        return ResponseEntity.ok(ApiResponse.ok("Vault closed",
                sessionService.closeVault(id, userId, method, http.getRemoteAddr())));
    }
}
