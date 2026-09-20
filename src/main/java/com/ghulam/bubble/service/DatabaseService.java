package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.DatabaseInfo;
import com.ghulam.bubble.dtos.HealthStatus;
import com.ghulam.bubble.repo.DatabaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

import static com.ghulam.bubble.helper.AppSetting.LOGGER;

@Service
public class DatabaseService {

    private final DatabaseRepository databaseRepository;

    public DatabaseService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public DatabaseInfo getDatabaseInfo() {
        return databaseRepository.getDatabaseInfo();
    }

    public HealthStatus getHealthStatus() {
        try {
            boolean connected = databaseRepository.isConnected();
            if (connected) {
                DatabaseInfo info = databaseRepository.getDatabaseInfo();
                return HealthStatus.builder()
                        .status("UP")
                        .database(info.databaseName())
                        .postgresVersion(info.postgresVersion())
                        .connectionPool(Collections.emptyMap())
                        .timestamp(Instant.now())
                        .build();
            }
        } catch (Exception e) {
            LOGGER("Health check failed", e);
        }

        return HealthStatus.builder()
                .status("DOWN")
                .timestamp(Instant.now())
                .build();
    }

    public String getVersion() {
        return databaseRepository.getPostgresVersion();
    }
}