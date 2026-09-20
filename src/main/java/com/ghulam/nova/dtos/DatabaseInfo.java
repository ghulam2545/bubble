package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record DatabaseInfo(
        String databaseName,
        String currentUser,
        String currentSchema,
        String postgresVersion,
        int serverVersionNum,
        String serverStartTime,
        String uptime,
        int activeConnections,
        int maxConnections,
        String timezone) {
}