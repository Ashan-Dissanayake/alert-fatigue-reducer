CREATE INDEX idx_incidents_status_environment
    ON incidents (status, environment);

CREATE INDEX idx_incidents_service_environment
    ON incidents (service, environment);

CREATE INDEX idx_incidents_last_updated_at
    ON incidents (last_updated_at);