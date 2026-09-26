package com.ghulam.nova.service;

import com.ghulam.nova.dtos.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import static com.ghulam.nova.helper.AppSetting.LOGGER;

@Slf4j
@Service
public class ConnectionService {

    private HikariDataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;

    public synchronized void connect(DatabaseConfig config) {
        String jdbcUrl = "jdbc:postgresql://%s:%d/%s".formatted(config.host(), config.port(), config.database());
        String username = config.username();
        String password = config.password();

        LOGGER(String.format("Connecting to Postgres --> %s as %s", jdbcUrl, username));

        HikariDataSource newDataSource = new HikariDataSource();
        newDataSource.setJdbcUrl(jdbcUrl);
        newDataSource.setUsername(username);
        newDataSource.setPassword(password);

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
                LOGGER("Previous database connection closed.");
            }

            LOGGER(String.format("Connected to '%s' successfully.", config.database()));
        } catch (Exception e) {
            // New connection failed, so clean it up.
            newDataSource.close();
            LOGGER(String.format("Failed to connect to '%s' successfully.", config.database()));
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
            LOGGER("Closing database connection.");

            dataSource.close();
            dataSource = null;
            jdbcTemplate = null;
        }
    }
}