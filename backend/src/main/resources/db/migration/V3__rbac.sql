-- ============================================================
-- V3__rbac.sql
-- users, roles, permissions, user_roles, role_permissions
-- ============================================================

CREATE TABLE permissions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    module      VARCHAR(50)  NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    deleted_at  TIMESTAMPTZ,
    created_by  UUID,
    updated_by  UUID
);

CREATE INDEX idx_permissions_module ON permissions(module);

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    is_system   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ,
    deleted_at  TIMESTAMPTZ,
    created_by  UUID,
    updated_by  UUID
);

CREATE TABLE role_permissions (
    role_id        UUID NOT NULL REFERENCES roles(id)       ON DELETE CASCADE,
    permission_id  UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id    UUID REFERENCES organizations(id) ON DELETE SET NULL,
    branch_id          UUID REFERENCES branches(id)      ON DELETE SET NULL,
    username           VARCHAR(64)  NOT NULL UNIQUE,
    email              CITEXT       NOT NULL UNIQUE,
    phone              VARCHAR(30),
    full_name          VARCHAR(150) NOT NULL,
    password           VARCHAR(255) NOT NULL,
    nik                VARCHAR(40),
    employee_id        VARCHAR(40),
    avatar_url         VARCHAR(500),
    is_enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked          BOOLEAN NOT NULL DEFAULT FALSE,
    locked_until       TIMESTAMPTZ,
    failed_attempts    INT NOT NULL DEFAULT 0,
    last_login_at      TIMESTAMPTZ,
    last_login_ip      VARCHAR(64),
    password_changed_at TIMESTAMPTZ,
    metadata           JSONB,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ,
    deleted_at         TIMESTAMPTZ,
    created_by         UUID,
    updated_by         UUID
);

CREATE INDEX idx_users_branch ON users(branch_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_org    ON users(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_active ON users(is_enabled) WHERE deleted_at IS NULL;

CREATE TABLE user_roles (
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE working_times (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week  SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6),
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE INDEX idx_working_times_user ON working_times(user_id);
