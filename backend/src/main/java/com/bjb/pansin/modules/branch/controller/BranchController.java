package com.bjb.pansin.modules.branch.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.branch.dto.BranchRequest;
import com.bjb.pansin.modules.branch.dto.BranchResponse;
import com.bjb.pansin.modules.branch.entity.Branch;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.organization.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Branches")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/branches")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchController {

    private final BranchRepository branchRepository;
    private final OrganizationRepository organizationRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<BranchResponse>>> list() {
        List<BranchResponse> branches = branchRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(branches));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BranchResponse>> get(@PathVariable UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(branch)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    @Transactional
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody BranchRequest req) {
        Branch branch = Branch.builder()
                .organization(organizationRepository.findById(req.getOrganizationId())
                        .orElseThrow(() -> new ResourceNotFoundException("Organization", req.getOrganizationId())))
                .code(req.getCode()).name(req.getName())
                .address(req.getAddress()).city(req.getCity()).province(req.getProvince())
                .postalCode(req.getPostalCode()).phone(req.getPhone()).email(req.getEmail())
                .latitude(req.getLatitude()).longitude(req.getLongitude())
                .timezone(req.getTimezone() != null ? req.getTimezone() : "Asia/Jakarta")
                .active(true).build();
        return ResponseEntity.ok(ApiResponse.ok("Branch created", toResponse(branchRepository.save(branch))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN_PUSAT')")
    @Transactional
    public ResponseEntity<ApiResponse<BranchResponse>> update(@PathVariable UUID id,
                                                       @Valid @RequestBody BranchRequest req) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branch.setName(req.getName());
        branch.setAddress(req.getAddress());
        branch.setCity(req.getCity());
        branch.setProvince(req.getProvince());
        branch.setPostalCode(req.getPostalCode());
        branch.setPhone(req.getPhone());
        branch.setEmail(req.getEmail());
        branch.setLatitude(req.getLatitude());
        branch.setLongitude(req.getLongitude());
        if (req.getTimezone() != null) branch.setTimezone(req.getTimezone());
        return ResponseEntity.ok(ApiResponse.ok("Branch updated", toResponse(branchRepository.save(branch))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", id));
        branchRepository.delete(branch);
        return ResponseEntity.ok(ApiResponse.ok("Branch deleted", null));
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .organizationId(branch.getOrganization() != null ? branch.getOrganization().getId() : null)
                .organization(branch.getOrganization() != null
                        ? BranchResponse.OrganizationSummary.builder()
                            .id(branch.getOrganization().getId())
                            .code(branch.getOrganization().getCode())
                            .name(branch.getOrganization().getName())
                            .build()
                        : null)
                .code(branch.getCode())
                .name(branch.getName())
                .address(branch.getAddress())
                .city(branch.getCity())
                .province(branch.getProvince())
                .postalCode(branch.getPostalCode())
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .timezone(branch.getTimezone())
                .active(branch.isActive())
                .createdAt(branch.getCreatedAt())
                .updatedAt(branch.getUpdatedAt())
                .build();
    }
}
