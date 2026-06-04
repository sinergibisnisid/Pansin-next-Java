-- ============================================================
-- V8__device_provisioning.sql
-- Hardware certification & secure provisioning
-- ============================================================

CREATE TABLE device_lifecycle_states (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id       UUID NOT NULL REFERENCES devices(id) ON DELETE CASCADE,
    state           VARCHAR(40) NOT NULL,
    previous_state  VARCHAR(40),
    reason          TEXT,
    actor_id        UUID REFERENCES users(id) ON DELETE SET NULL,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lifecycle_device ON device_lifecycle_states(device_id, created_at DESC);
CREATE INDEX idx_lifecycle_state  ON device_lifecycle_states(state);

CREATE TABLE device_certificates (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id             UUID REFERENCES devices(id) ON DELETE SET NULL,
    serial_number         VARCHAR(80)  NOT NULL UNIQUE,
    subject_dn            VARCHAR(255) NOT NULL,
    issuer_dn             VARCHAR(255) NOT NULL,
    fingerprint_sha256    VARCHAR(80)  NOT NULL UNIQUE,
    public_key_pem        TEXT,
    certificate_pem       TEXT NOT NULL,
    not_before            TIMESTAMPTZ NOT NULL,
    not_after             TIMESTAMPTZ NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    revoked_at            TIMESTAMPTZ,
    revoked_reason        VARCHAR(40),
    revoked_by            UUID REFERENCES users(id) ON DELETE SET NULL,
    issued_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    metadata              JSONB,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ,
    deleted_at            TIMESTAMPTZ,
    created_by            UUID,
    updated_by            UUID
);

CREATE INDEX idx_cert_device      ON device_certificates(device_id);
CREATE INDEX idx_cert_status      ON device_certificates(status);
CREATE INDEX idx_cert_fingerprint ON device_certificates(fingerprint_sha256);

-- Add lifecycle state column to devices for fast lookup
ALTER TABLE devices
    ADD COLUMN lifecycle_state VARCHAR(40) NOT NULL DEFAULT 'MANUFACTURED';

CREATE INDEX idx_devices_lifecycle ON devices(lifecycle_state);
