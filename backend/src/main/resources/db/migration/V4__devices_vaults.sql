-- ============================================================
-- V4__devices_vaults.sql
-- vaults, devices, fingerprint_devices
-- ============================================================

CREATE TABLE vaults (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id       UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    code            VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    location        VARCHAR(255),
    status          VARCHAR(30)  NOT NULL DEFAULT 'CLOSED',
    last_opened_at  TIMESTAMPTZ,
    last_closed_at  TIMESTAMPTZ,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_vaults_branch ON vaults(branch_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_vaults_status ON vaults(status);

CREATE TABLE devices (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id         UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    vault_id          UUID REFERENCES vaults(id) ON DELETE SET NULL,
    device_code       VARCHAR(80)  NOT NULL UNIQUE,
    name              VARCHAR(150) NOT NULL,
    type              VARCHAR(40)  NOT NULL,
    ip_address        VARCHAR(64),
    mac_address       VARCHAR(64),
    firmware_version  VARCHAR(40),
    signal_quality    INT,
    status            VARCHAR(30) NOT NULL DEFAULT 'OFFLINE',
    last_heartbeat    TIMESTAMPTZ,
    metadata          JSONB,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ,
    deleted_at        TIMESTAMPTZ,
    created_by        UUID,
    updated_by        UUID
);

CREATE INDEX idx_devices_branch ON devices(branch_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_devices_vault  ON devices(vault_id);
CREATE INDEX idx_devices_status ON devices(status);

CREATE TABLE fingerprint_devices (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    vault_id        UUID REFERENCES vaults(id) ON DELETE SET NULL,
    serial_number   VARCHAR(120) NOT NULL UNIQUE,
    model           VARCHAR(80),
    capacity        INT,
    enrolled_count  INT NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_fp_devices_device ON fingerprint_devices(device_id);
