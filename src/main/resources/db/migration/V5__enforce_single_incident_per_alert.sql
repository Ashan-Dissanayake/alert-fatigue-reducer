ALTER TABLE incident_alerts
    ADD CONSTRAINT uk_incident_alerts_alert_id UNIQUE (alert_id);