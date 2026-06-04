-- ============================================================
-- V5__operations_logs.sql
-- vault_sessions, vault_access_logs, fingerprint_logs,
-- alarm_logs, mqtt_logs, device_heartbeats
-- ============================================================

CREATE TABLE vault_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id        UUID NOT NULL REFERENCES vaults(id) ON DELETE RESTRICT,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    opened_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at       TIMESTAMPTZ,
    duration_seconds INT,
    status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    open_method     VARCHAR(40),
    close_method    VARCHAR(40),
    notes           TEXT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_vault_sessions_vault   ON vault_sessions(vault_id, opened_at DESC);
CREATE INDEX idx_vault_sessions_user    ON vault_sessions(user_id);
CREATE INDEX idx_vault_sessions_status  ON vault_sessions(status);

CREATE TABLE vault_access_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id    UUID NOT NULL REFERENCES vaults(id) ON DELETE CASCADE,
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    session_id  UUID REFERENCES vault_sessions(id) ON DELETE SET NULL,
    action      VARCHAR(40) NOT NULL,
    success     BOOLEAN NOT NULL DEFAULT TRUE,
    method      VARCHAR(40),
    source_ip   VARCHAR(64),
    metadata    JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vault_access_vault ON vault_access_logs(vault_id, created_at DESC);
CREATE INDEX idx_vault_access_user  ON vault_access_logs(user_id);

CREATE TABLE fingerprint_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID REFERENCES devices(id) ON DELETE SET NULL,
    fp_device_id    UUID REFERENCES fingerprint_devices(id) ON DELETE SET NULL,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    template_id     VARCHAR(120),
    quality_score   INT,
    matched         BOOLEAN NOT NULL DEFAULT FALSE,
    reason          VARCHAR(120),
    raw_payload     JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_fp_logs_user   ON fingerprint_logs(user_id);
CREATE INDEX idx_fp_logs_device ON fingerprint_logs(device_id, created_at DESC);

CREATE TABLE alarm_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id     UUID REFERENCES vaults(id) ON DELETE SET NULL,
    device_id    UUID REFERENCES devices(id) ON DELETE SET NULL,
    session_id   UUID REFERENCES vault_sessions(id) ON DELETE SET NULL,
    type         VARCHAR(40) NOT NULL,
    severity     VARCHAR(20) NOT NULL DEFAULT 'HIGH',
    message      TEXT,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by UUID REFERENCES users(id) ON DELETE SET NULL,
    acknowledged_at TIMESTAMPTZ,
    metadata     JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_alarm_vault ON alarm_logs(vault_id, created_at DESC);
CREATE INDEX idx_alarm_type  ON alarm_logs(type);

CREATE TABLE mqtt_logs (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic       VARCHAR(255) NOT NULL,
    direction   VARCHAR(10)  NOT NULL,
    qos         SMALLINT     NOT NULL DEFAULT 0,
    payload     JSONB,
    client_id   VARCHAR(120),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_mqtt_logs_topic ON mqtt_logs(topic, created_at DESC);

CREATE TABLE device_heartbeats (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    cpu_load        DECIMAL(5,2),
    memory_load     DECIMAL(5,2),
    signal_quality  INT,
    uptime_seconds  BIGINT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_heartbeats_device ON device_heartbeats(device_id, created_at DESC);
