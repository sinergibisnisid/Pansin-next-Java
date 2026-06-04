-- ============================================================
-- V7__audit_reports_monitoring.sql
-- audit_logs, activity_logs, reports, server_monitorings
-- ============================================================

CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    actor_name   VARCHAR(150),
    action       VARCHAR(80) NOT NULL,
    entity_type  VARCHAR(80),
    entity_id    UUID,
    description  TEXT,
    before_data  JSONB,
    after_data   JSONB,
    ip_address   VARCHAR(64),
    user_agent   VARCHAR(255),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_actor ON audit_logs(actor_id, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);

CREATE TABLE activity_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    activity     VARCHAR(120) NOT NULL,
    description  TEXT,
    ip_address   VARCHAR(64),
    user_agent   VARCHAR(255),
    metadata     JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_user ON activity_logs(user_id, created_at DESC);

CREATE TABLE reports (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(150) NOT NULL,
    type          VARCHAR(40)  NOT NULL,
    format        VARCHAR(10)  NOT NULL,
    parameters    JSONB,
    file_path     VARCHAR(500),
    file_size     BIGINT,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_by  UUID REFERENCES users(id) ON DELETE SET NULL,
    generated_at  TIMESTAMPTZ,
    error_message TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ
);

CREATE INDEX idx_reports_status ON reports(status, created_at DESC);

CREATE TABLE server_monitorings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostname        VARCHAR(120),
    cpu_load        DECIMAL(5,2),
    memory_load     DECIMAL(5,2),
    disk_load       DECIMAL(5,2),
    mqtt_connected  BOOLEAN,
    websocket_count INT,
    queue_size      INT,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_server_monitor_created ON server_monitorings(created_at DESC);
