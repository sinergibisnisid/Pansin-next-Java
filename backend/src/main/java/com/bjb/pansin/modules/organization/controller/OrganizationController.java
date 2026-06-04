package com.bjb.pansin.modules.organization.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.organization.dto.OrganizationRequest;
import com.bjb.pansin.modules.organization.entity.Organization;
import com.bjb.pansin.modules.organization.repository.OrganizationRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Organizations")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationRepository repository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Organization>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repository.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Organization>> getById(@PathVariable UUID id) {
        Organization org = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
        return ResponseEntity.ok(ApiResponse.ok("Organization retrieved", org));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> create(@Valid @RequestBody OrganizationRequest req) {
        Organization org = Organization.builder()
                .code(req.getCode()).name(req.getName())
                .description(req.getDescription()).address(req.getAddress())
                .phone(req.getPhone()).email(req.getEmail())
                .active(true).build();
        return ResponseEntity.ok(ApiResponse.ok("Organization created", repository.save(org)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody OrganizationRequest req) {
        Organization org = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
        org.setName(req.getName());
        org.setDescription(req.getDescription());
        org.setAddress(req.getAddress());
        org.setPhone(req.getPhone());
        org.setEmail(req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Organization updated", repository.save(org)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Organization org = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
        repository.delete(org);
        return ResponseEntity.ok(ApiResponse.ok("Organization deleted", null));
    }
}
