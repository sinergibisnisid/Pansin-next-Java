package com.bjb.pansin.modules.user.controller;

import com.bjb.pansin.common.constants.AppConstants;
import com.bjb.pansin.common.dto.ApiResponse;
import com.bjb.pansin.common.dto.PageResponse;
import com.bjb.pansin.modules.user.dto.CreateUserRequest;
import com.bjb.pansin.modules.user.dto.UpdateUserRequest;
import com.bjb.pansin.modules.user.dto.UserResponse;
import com.bjb.pansin.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users")
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> list(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.of(userService.list(pageable))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.get(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.create(req)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.update(id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}
