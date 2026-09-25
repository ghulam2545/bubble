package com.ghulam.nova.service;

import com.ghulam.nova.dtos.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionService {

    private HikariDataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;

    public synchronized void connect(DatabaseConfig config) {
        String jdbcUrl = "jdbc:postgresql://%s:%d/%s".formatted(config.host(), config.port(), config.database());

        log.info("Connecting to Postgres --> {} as '{}'", jdbcUrl, config.username());

        HikariDataSource newDataSource = new HikariDataSource();
        newDataSource.setJdbcUrl(jdbcUrl);
        newDataSource.setUsername(config.username());
        newDataSource.setPassword(config.password());

        try {
            NamedParameterJdbcTemplate newJdbcTemplate = new NamedParameterJdbcTemplate(newDataSource);

            // verify the connection.
            newJdbcTemplate.getJdbcTemplate().queryForObject(
                    "SELECT 1",
                    Integer.class
            );

            // New connection is valid, so replace the old one.
            HikariDataSource oldDataSource = this.dataSource;

            this.dataSource = newDataSource;
            this.jdbcTemplate = newJdbcTemplate;
            if (oldDataSource != null) {
                oldDataSource.close();
                log.debug("Previous database connection closed");
            }

            log.info("Connected to '{}' successfully", config.database());
        } catch (Exception e) {
            // New connection failed, so clean it up.
            newDataSource.close();
            log.error("Failed to connect to PostgreSQL database '{}'", config.database(), e);
            throw new IllegalStateException("Failed to connect to database: " + config.database(), e);
        }
    }

    public NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate template = this.jdbcTemplate;

        if (template == null) {
            throw new IllegalStateException("Database is not connected");
        }

        return template;
    }

    @PreDestroy
    public synchronized void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Closing database connection");

            dataSource.close();
            dataSource = null;
            jdbcTemplate = null;
        }
    }
}