package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record FunctionInfo(
        String name,
        String schema,
        String arguments,
        String returnType,
        String language,
        String volatility,
        String parallelSafety,
        boolean securityDefiner,
        String definition,
        String owner) {
}