package com.bjb.pansin.modules.snapshot.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.snapshot.entity.Snapshot;
import com.bjb.pansin.modules.snapshot.repository.SnapshotRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Tag(name = "Snapshots")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/snapshots")
@RequiredArgsConstructor
public class SnapshotController {

    private final SnapshotRepository repository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<Snapshot>>> list(
            @RequestParam(required = false) UUID vaultId,
            @ParameterObject Pageable pageable) {
        var page = vaultId != null
                ? repository.findByVaultId(vaultId, pageable)
                : repository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        Snapshot s = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot", id));
        Path path = Path.of(s.getFilePath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Snapshot file missing on disk: " + s.getFilePath());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + path.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(s.getMimeType() != null ? s.getMimeType() : "image/jpeg"))
                .body(new FileSystemResource(path));
    }
}
