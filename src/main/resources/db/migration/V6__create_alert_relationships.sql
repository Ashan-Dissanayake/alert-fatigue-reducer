
CREATE TABLE alert_relationships (
                                     id BIGSERIAL PRIMARY KEY,

                                     source_type VARCHAR(100) NOT NULL,
                                     related_type VARCHAR(100) NOT NULL,

                                     CONSTRAINT uk_alert_relationships_types
                                         UNIQUE (source_type, related_type),

                                     CONSTRAINT chk_alert_relationships_different_types
                                         CHECK (source_type <> related_type)
);

INSERT INTO alert_relationships (source_type, related_type)
VALUES
    ('CPU_HIGH', 'LATENCY_HIGH'),
    ('LATENCY_HIGH', 'CPU_HIGH'),
    ('LATENCY_HIGH', 'ERROR_RATE_HIGH'),
    ('ERROR_RATE_HIGH', 'LATENCY_HIGH'),
    ('ERROR_RATE_HIGH', 'TIMEOUT'),
    ('TIMEOUT', 'ERROR_RATE_HIGH');
