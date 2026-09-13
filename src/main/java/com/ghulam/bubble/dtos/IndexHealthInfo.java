package com.ghulam.bubble.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record IndexHealthInfo(
        String indexName,
        String schema,
        String table,
        String status,
        List<String> issues,
        List<String> recommendations,
        String sizePretty,
        long indexScans
) {
}
