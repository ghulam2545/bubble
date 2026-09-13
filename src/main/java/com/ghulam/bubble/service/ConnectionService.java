package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ConnectionService {

    private NamedParameterJdbcTemplate jdbcTemplate;

    public void connect(DatabaseConfig config) {
        HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(
                "jdbc:postgresql://%s:%d/%s"
                        .formatted(config.host(), config.port(), config.database())
        );
        dataSource.setUsername(config.username());
        dataSource.setPassword(config.password());

        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public NamedParameterJdbcTemplate jdbc() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("Database is not connected");
        }

        return jdbcTemplate;
    }
}