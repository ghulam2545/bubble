package com.ghulam.nova.service;

import com.ghulam.nova.dtos.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConnectionService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public void connect(DatabaseConfig config) {
        String jdbcUrl = "jdbc:postgresql://%s:%d/%s"
                .formatted(config.host(), config.port(), config.database());

        log.info("Connecting to PostgreSQL --> {} as '{}'", jdbcUrl, config.username());

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(config.username());
        dataSource.setPassword(config.password());

        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        log.info("Connected to '{}' successfully", config.database());
    }

    public NamedParameterJdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("Database is not connected");
        }

        return jdbcTemplate;
    }
}