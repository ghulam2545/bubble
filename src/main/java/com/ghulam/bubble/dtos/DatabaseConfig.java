package com.ghulam.bubble.dtos;

public record DatabaseConfig(
        String host,
        int port,
        String database,
        String username,
        String password
) {
}