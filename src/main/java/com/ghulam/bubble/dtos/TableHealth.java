package com.ghulam.bubble.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record TableHealth(
        String schema,
        String table,
        String status,
        double deadTupleRatio,
        double seqScanRatio,
        double indexUsageRatio,
        List<String> issues,
        List<String> recommendations) {
}