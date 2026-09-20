package com.ghulam.nova.dtos;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password
) {
}