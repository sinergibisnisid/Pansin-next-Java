package com.bjb.pansin.modules.user.service;

import com.bjb.pansin.common.exceptions.BusinessException;
import com.bjb.pansin.common.exceptions.ResourceNotFoundException;
import com.bjb.pansin.modules.branch.entity.Branch;
import com.bjb.pansin.modules.branch.repository.BranchRepository;
import com.bjb.pansin.modules.organization.entity.Organization;
import com.bjb.pansin.modules.organization.repository.OrganizationRepository;
import com.bjb.pansin.modules.role.entity.Role;
import com.bjb.pansin.modules.role.repository.RoleRepository;
import com.bjb.pansin.modules.user.dto.CreateUserRequest;
import com.bjb.pansin.modules.user.dto.UpdateUserRequest;
import com.bjb.pansin.modules.user.dto.UserResponse;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.mapper.UserMapper;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return UserMapper.toResponse(findById(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException("USERNAME_TAKEN", "Username already exists");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("EMAIL_TAKEN", "Email already exists");
        }

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .nik(req.getNik())
                .employeeId(req.getEmployeeId())
                .passwordChangedAt(Instant.now())
                .build();

        if (req.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(req.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", req.getOrganizationId()));
            user.setOrganization(org);
        }
        if (req.getBranchId() != null) {
            Branch branch = branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId()));
            user.setBranch(branch);
        }

        if (req.getRoleCodes() != null && !req.getRoleCodes().isEmpty()) {
            user.setRoles(resolveRoles(req.getRoleCodes()));
        }

        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest req) {
        User user = findById(id);

        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new BusinessException("EMAIL_TAKEN", "Email already exists");
            }
            user.setEmail(req.getEmail());
        }
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPhone() != null)    user.setPhone(req.getPhone());
        if (req.getNik() != null)      user.setNik(req.getNik());
        if (req.getEmployeeId() != null) user.setEmployeeId(req.getEmployeeId());
        if (req.getEnabled() != null)  user.setEnabled(req.getEnabled());

        if (req.getOrganizationId() != null) {
            user.setOrganization(organizationRepository.findById(req.getOrganizationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", req.getOrganizationId())));
        }
        if (req.getBranchId() != null) {
            user.setBranch(branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", req.getBranchId())));
        }
        if (req.getRoleCodes() != null) {
            user.setRoles(resolveRoles(req.getRoleCodes()));
        }
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(UUID id) {
        User user = findById(id);
        user.setDeletedAt(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID id, String currentPassword, String newPassword) {
        User user = findById(id);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("INVALID_PASSWORD", "Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());
        userRepository.save(user);
    }

    private User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private Set<Role> resolveRoles(Set<String> codes) {
        Set<Role> roles = new HashSet<>();
        for (String code : codes) {
            roles.add(roleRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", code)));
        }
        return roles;
    }
}
