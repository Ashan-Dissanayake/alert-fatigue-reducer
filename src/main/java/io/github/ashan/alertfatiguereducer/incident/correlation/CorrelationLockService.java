package io.github.ashan.alertfatiguereducer.incident.correlation;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;

@Service
@RequiredArgsConstructor
public class CorrelationLockService {

    private static final int LOCK_NAMESPACE = 741205;

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void lockEnvironment(AlertEnvironment environment) {
        int lockKey = environment.name().hashCode();

        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_advisory_xact_lock(?, ?)"
            )) {
                statement.setInt(1, LOCK_NAMESPACE);
                statement.setInt(2, lockKey);
                statement.execute();
            }

            return null;
        });
    }
}