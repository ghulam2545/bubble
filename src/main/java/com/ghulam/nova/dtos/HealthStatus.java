package com.ghulam.nova.dtos;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;

@Builder
public record HealthStatus(
        String status,
        String database,
        String postgresVersion,
        Map<String, Object> connectionPool,
        Instant timestamp) {
}