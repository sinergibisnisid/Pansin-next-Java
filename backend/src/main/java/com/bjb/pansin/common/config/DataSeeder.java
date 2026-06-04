package com.bjb.pansin.common.config;

import com.bjb.pansin.common.enums.RoleType;
import com.bjb.pansin.modules.organization.entity.Organization;
import com.bjb.pansin.modules.organization.repository.OrganizationRepository;
import com.bjb.pansin.modules.permission.entity.Permission;
import com.bjb.pansin.modules.permission.repository.PermissionRepository;
import com.bjb.pansin.modules.role.entity.Role;
import com.bjb.pansin.modules.role.repository.RoleRepository;
import com.bjb.pansin.modules.user.entity.User;
import com.bjb.pansin.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Value("${app.seed.super-admin-username:superadmin}")
    private String superAdminUsername;

    @Value("${app.seed.super-admin-email:superadmin@pansin.local}")
    private String superAdminEmail;

    @Value("${app.seed.super-admin-password:Pansin@2024!}")
    private String superAdminPassword;

    private static final List<String[]> PERMISSIONS = List.of(
            new String[]{"USER_READ",        "User",        "Read users"},
            new String[]{"USER_CREATE",      "User",        "Create users"},
            new String[]{"USER_UPDATE",      "User",        "Update users"},
            new String[]{"USER_DELETE",      "User",        "Delete users"},
            new String[]{"ROLE_READ",        "Role",        "Read roles"},
            new String[]{"ROLE_MANAGE",      "Role",        "Manage roles"},
            new String[]{"PERMISSION_READ",  "Permission",  "Read permissions"},
            new String[]{"BRANCH_READ",      "Branch",      "Read branches"},
            new String[]{"BRANCH_MANAGE",    "Branch",      "Manage branches"},
            new String[]{"VAULT_READ",       "Vault",       "Read vaults"},
            new String[]{"VAULT_MANAGE",     "Vault",       "Manage vaults"},
            new String[]{"VAULT_OPEN",       "Vault",       "Open vault"},
            new String[]{"VAULT_CLOSE",      "Vault",       "Close vault"},
            new String[]{"DEVICE_READ",      "Device",      "Read devices"},
            new String[]{"DEVICE_MANAGE",    "Device",      "Manage devices"},
            new String[]{"FINGERPRINT_MANAGE","Fingerprint","Manage fingerprints"},
            new String[]{"ALARM_VIEW",       "Alarm",       "View alarms"},
            new String[]{"ALARM_ACK",        "Alarm",       "Acknowledge alarms"},
            new String[]{"REPORT_EXPORT",    "Report",      "Export reports"},
            new String[]{"AUDIT_VIEW",       "Audit",       "View audit logs"},
            new String[]{"MAINTENANCE_MANAGE","Maintenance","Manage maintenance"},
            new String[]{"LIVESTREAM_VIEW",  "Livestream",  "View livestream"},
            new String[]{"NOTIFICATION_MANAGE","Notification","Manage notifications"}
    );

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;

        seedPermissions();
        seedRoles();
        seedDefaultOrganization();
        seedSuperAdmin();

        log.info("Seed completed. Default super admin: {} (change password on first login)", superAdminUsername);
    }

    private void seedPermissions() {
        for (String[] p : PERMISSIONS) {
            permissionRepository.findByCode(p[0]).orElseGet(() ->
                    permissionRepository.save(Permission.builder()
                            .code(p[0]).module(p[1]).name(p[0]).description(p[2]).build()));
        }
    }

    private void seedRoles() {
        Set<Permission> all = new HashSet<>(permissionRepository.findAll());

        for (RoleType rt : RoleType.values()) {
            roleRepository.findByCode(rt.name()).orElseGet(() -> {
                Role role = Role.builder()
                        .code(rt.name())
                        .name(humanize(rt.name()))
                        .description("Built-in role: " + rt.name())
                        .system(true)
                        .build();
                role.setPermissions(switch (rt) {
                    case SUPER_ADMIN -> all;
                    case ADMIN_PUSAT -> filterPerms(all, "USER", "ROLE", "BRANCH", "VAULT", "DEVICE",
                            "FINGERPRINT", "REPORT", "AUDIT", "MAINTENANCE", "ALARM", "LIVESTREAM",
                            "NOTIFICATION", "PERMISSION");
                    case ADMIN_CABANG -> filterPerms(all, "USER_READ", "USER_CREATE", "USER_UPDATE",
                            "BRANCH_READ", "VAULT", "DEVICE", "FINGERPRINT", "REPORT_EXPORT",
                            "ALARM", "LIVESTREAM_VIEW", "MAINTENANCE_MANAGE");
                    case OPERATOR -> filterPerms(all, "VAULT_READ", "VAULT_OPEN", "VAULT_CLOSE",
                            "DEVICE_READ", "ALARM_VIEW", "LIVESTREAM_VIEW");
                    case SECURITY -> filterPerms(all, "VAULT_READ", "ALARM_VIEW", "ALARM_ACK",
                            "LIVESTREAM_VIEW", "DEVICE_READ");
                    case MAINTENANCE -> filterPerms(all, "DEVICE_READ", "MAINTENANCE_MANAGE",
                            "VAULT_READ", "ALARM_VIEW");
                    case VIEWER -> filterPerms(all, "VAULT_READ", "DEVICE_READ", "ALARM_VIEW",
                            "REPORT_EXPORT", "AUDIT_VIEW");
                });
                return roleRepository.save(role);
            });
        }
    }

    private Set<Permission> filterPerms(Set<Permission> all, String... prefixes) {
        Set<Permission> result = new HashSet<>();
        for (Permission p : all) {
            for (String pre : prefixes) {
                if (p.getCode().equals(pre) || p.getCode().startsWith(pre + "_") || p.getCode().startsWith(pre)) {
                    result.add(p);
                    break;
                }
            }
        }
        return result;
    }

    private void seedDefaultOrganization() {
        organizationRepository.findByCode("BJB").orElseGet(() ->
                organizationRepository.save(Organization.builder()
                        .code("BJB").name("Bank BJB").description("Default organization")
                        .active(true).build()));
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByUsername(superAdminUsername)) return;

        Role superRole = roleRepository.findByCode(RoleType.SUPER_ADMIN.name())
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role missing"));

        Set<Role> roles = new HashSet<>();
        roles.add(superRole);

        userRepository.save(User.builder()
                .username(superAdminUsername)
                .email(superAdminEmail)
                .password(passwordEncoder.encode(superAdminPassword))
                .fullName("Super Administrator")
                .organization(organizationRepository.findByCode("BJB").orElse(null))
                .enabled(true)
                .locked(false)
                .roles(roles)
                .passwordChangedAt(Instant.now())
                .build());
    }

    private String humanize(String code) {
        String[] parts = code.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
