package com.bjb.pansin.modules.role.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.permission.entity.Permission;
import com.bjb.pansin.modules.permission.repository.PermissionRepository;
import com.bjb.pansin.modules.role.dto.RoleRequest;
import com.bjb.pansin.modules.role.dto.RoleResponse;
import com.bjb.pansin.modules.role.entity.Role;
import com.bjb.pansin.modules.role.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoleResponse create(RoleRequest req) {
        if (roleRepository.existsByCode(req.getCode())) {
            throw new BusinessException("ROLE_EXISTS", "Role code already exists");
        }
        Role role = Role.builder()
                .code(req.getCode())
                .name(req.getName())
                .description(req.getDescription())
                .permissions(resolvePermissions(req.getPermissionCodes()))
                .build();
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest req) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if (role.isSystem()) {
            throw new BusinessException("ROLE_SYSTEM", "System role cannot be modified");
        }
        role.setName(req.getName());
        role.setDescription(req.getDescription());
        role.setPermissions(resolvePermissions(req.getPermissionCodes()));
        return toResponse(roleRepository.save(role));
    }

    @Transactional
    public void delete(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", id));
        if (role.isSystem()) {
            throw new BusinessException("ROLE_SYSTEM", "System role cannot be deleted");
        }
        roleRepository.delete(role);
    }

    private Set<Permission> resolvePermissions(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return new HashSet<>();
        return new HashSet<>(permissionRepository.findByCodeIn(codes));
    }

    private RoleResponse toResponse(Role r) {
        return RoleResponse.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .description(r.getDescription())
                .system(r.isSystem())
                .permissions(r.getPermissions().stream().map(Permission::getCode).collect(Collectors.toSet()))
                .build();
    }
}
