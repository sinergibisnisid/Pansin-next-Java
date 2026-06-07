CREATE TABLE app_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value JSONB NOT NULL,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,
    deleted_at TIMESTAMPTZ,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_app_settings_key ON app_settings(setting_key);
CREATE INDEX idx_app_settings_public ON app_settings(is_public);

INSERT INTO app_settings (setting_key, setting_value, description, is_public)
VALUES
    ('app.general', '{"appName":"PANSIN ACCESS","organizationName":"Bank BJB","timezone":"Asia/Jakarta","language":"id"}'::jsonb, 'General application settings', TRUE),
    ('app.security', '{"sessionTimeoutMinutes":30,"otpExpiryMinutes":5,"maxLoginAttempts":5,"passwordMinLength":8}'::jsonb, 'Security settings', FALSE),
    ('app.notification', '{"emailEnabled":true,"whatsappEnabled":true,"alarmNotification":true,"maintenanceReminder":true}'::jsonb, 'Notification settings', FALSE),
    ('app.system', '{"maintenanceMode":false,"metricsRefreshSeconds":60,"apiBaseUrl":"/api/v1","websocketUrl":"/ws"}'::jsonb, 'System settings', FALSE);
