package com.ghulam.bubble.dtos;

import lombok.Builder;

@Builder
public record ActivitySession(
        int pid,
        String usename,
        String applicationName,
        String clientAddr,
        Integer clientPort,
        String backendStart,
        String xactStart,
        String queryStart,
        String stateChange,
        String waitEventType,
        String waitEvent,
        String state,
        String backendXid,
        String backendXmin,
        String query,
        Double durationSeconds) {
}