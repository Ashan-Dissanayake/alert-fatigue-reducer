CREATE TABLE alerts (
                        id BIGSERIAL PRIMARY KEY,
                        source VARCHAR(50) NOT NULL,
                        service VARCHAR(100) NOT NULL,
                        environment VARCHAR(50) NOT NULL,
                        type VARCHAR(100) NOT NULL,
                        severity VARCHAR(30) NOT NULL,
                        message TEXT,
                        timestamp TIMESTAMP NOT NULL,
                        metric VARCHAR(100),
                        value DOUBLE PRECISION,
                        threshold DOUBLE PRECISION,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE incidents (
                           id BIGSERIAL PRIMARY KEY,
                           title VARCHAR(255) NOT NULL,
                           service VARCHAR(100) NOT NULL,
                           environment VARCHAR(50) NOT NULL,
                           severity VARCHAR(30) NOT NULL,
                           status VARCHAR(30) NOT NULL,
                           started_at TIMESTAMP NOT NULL,
                           last_updated_at TIMESTAMP NOT NULL,
                           correlation_score DOUBLE PRECISION
);

CREATE TABLE incident_alerts (
                                 incident_id BIGINT NOT NULL,
                                 alert_id BIGINT NOT NULL,

                                 PRIMARY KEY (incident_id, alert_id),

                                 CONSTRAINT fk_incident_alerts_incident
                                     FOREIGN KEY (incident_id)
                                         REFERENCES incidents(id),

                                 CONSTRAINT fk_incident_alerts_alert
                                     FOREIGN KEY (alert_id)
                                         REFERENCES alerts(id)
);