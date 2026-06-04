-- ============================================================
-- V6__media_notification_maintenance.sql
-- livestream_sessions, snapshots, notification_logs,
-- notification_configs, maintenance_plans, maintenance_logs
-- ============================================================

CREATE TABLE livestream_sessions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id        UUID REFERENCES vaults(id) ON DELETE SET NULL,
    device_id       UUID REFERENCES devices(id) ON DELETE SET NULL,
    user_id         UUID REFERENCES users(id) ON DELETE SET NULL,
    session_token   VARCHAR(255) NOT NULL UNIQUE,
    stream_url      VARCHAR(500),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ended_at        TIMESTAMPTZ,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ
);

CREATE INDEX idx_livestream_user  ON livestream_sessions(user_id);
CREATE INDEX idx_livestream_vault ON livestream_sessions(vault_id);

CREATE TABLE snapshots (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id        UUID REFERENCES vaults(id) ON DELETE SET NULL,
    session_id      UUID REFERENCES vault_sessions(id) ON DELETE SET NULL,
    device_id       UUID REFERENCES devices(id) ON DELETE SET NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size       BIGINT,
    mime_type       VARCHAR(80),
    trigger         VARCHAR(40),
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_snapshots_vault ON snapshots(vault_id, created_at DESC);

CREATE TABLE notification_configs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id    UUID REFERENCES branches(id) ON DELETE CASCADE,
    channel      VARCHAR(20) NOT NULL,
    event_type   VARCHAR(60) NOT NULL,
    recipients   JSONB NOT NULL,
    template     TEXT,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE INDEX idx_notification_configs_event ON notification_configs(event_type, channel);

CREATE TABLE notification_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    channel      VARCHAR(20) NOT NULL,
    recipient    VARCHAR(255) NOT NULL,
    subject      VARCHAR(255),
    body         TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    related_type VARCHAR(40),
    related_id   UUID,
    sent_at      TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_notification_logs_status ON notification_logs(status, created_at DESC);
CREATE INDEX idx_notification_logs_related ON notification_logs(related_type, related_id);

CREATE TABLE maintenance_plans (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vault_id     UUID REFERENCES vaults(id) ON DELETE CASCADE,
    device_id    UUID REFERENCES devices(id) ON DELETE CASCADE,
    type         VARCHAR(40) NOT NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    interval_days INT NOT NULL,
    next_due_at  TIMESTAMPTZ,
    last_done_at TIMESTAMPTZ,
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ,
    created_by   UUID,
    updated_by   UUID
);

CREATE INDEX idx_maintenance_plans_due ON maintenance_plans(next_due_at) WHERE is_active = TRUE;

CREATE TABLE maintenance_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id      UUID REFERENCES maintenance_plans(id) ON DELETE SET NULL,
    vault_id     UUID REFERENCES vaults(id) ON DELETE SET NULL,
    device_id    UUID REFERENCES devices(id) ON DELETE SET NULL,
    performed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    type         VARCHAR(40) NOT NULL,
    notes        TEXT,
    status       VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    performed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata     JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_maintenance_logs_plan ON maintenance_logs(plan_id, performed_at DESC);
