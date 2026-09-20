package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.DatabaseInfo;
import com.ghulam.nova.dtos.HealthStatus;
import com.ghulam.nova.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping(path = "/backend/api/v1/activity")
public class SystemController {

    private final DatabaseService databaseService;

    public SystemController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("/health")
    public HealthStatus getHealth() {
        return databaseService.getHealthStatus();
    }

    @GetMapping("/database")
    public DatabaseInfo getDatabaseInfo() {
        return databaseService.getDatabaseInfo();
    }

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Collections.singletonMap("version", databaseService.getVersion());
    }

    @GetMapping("/connection")
    public Map<String, Object> getConnectionPool() {
        HealthStatus status = databaseService.getHealthStatus();
        return status.connectionPool() != null ? status.connectionPool() : Collections.emptyMap();
    }

    @GetMapping("/uptime")
    public Map<String, String> getUptime() {
        DatabaseInfo info = databaseService.getDatabaseInfo();
        return Collections.singletonMap("uptime", info.uptime());
    }
}