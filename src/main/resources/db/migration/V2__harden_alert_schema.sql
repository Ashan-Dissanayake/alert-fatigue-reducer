-- Remove redundant single-column indexes
DROP INDEX IF EXISTS idx_alerts_service_environment;
DROP INDEX IF EXISTS idx_alerts_timestamp;

-- Support correlation candidate and timeline lookups
CREATE INDEX idx_alerts_service_environment_timestamp
    ON alerts (service, environment, timestamp);

-- Application owns created_at assignment
ALTER TABLE alerts
    ALTER COLUMN created_at DROP DEFAULT;