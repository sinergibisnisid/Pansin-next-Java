-- ============================================================
-- V2__core_tenant.sql
-- organizations, branches
-- ============================================================

CREATE TABLE organizations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    address         TEXT,
    phone           VARCHAR(30),
    email           VARCHAR(150),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      UUID,
    updated_by      UUID
);

CREATE INDEX idx_organizations_active ON organizations(is_active) WHERE deleted_at IS NULL;

CREATE TABLE branches (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id  UUID NOT NULL REFERENCES organizations(id) ON DELETE RESTRICT,
    code             VARCHAR(50)  NOT NULL UNIQUE,
    name             VARCHAR(150) NOT NULL,
    address          TEXT,
    city             VARCHAR(100),
    province         VARCHAR(100),
    postal_code      VARCHAR(20),
    phone            VARCHAR(30),
    email            VARCHAR(150),
    latitude         DECIMAL(10,7),
    longitude        DECIMAL(10,7),
    timezone         VARCHAR(64) NOT NULL DEFAULT 'Asia/Jakarta',
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    metadata         JSONB,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ,
    deleted_at       TIMESTAMPTZ,
    created_by       UUID,
    updated_by       UUID
);

CREATE INDEX idx_branches_org ON branches(organization_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_branches_code ON branches(code);
