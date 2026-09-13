package com.ghulam.bubble.dtos;

import lombok.Builder;

@Builder
public record SchemaStorageInfo(
        String schema,
        long totalSizeBytes,
        String totalSizePretty,
        int tablesCount,
        int indexesCount
) {
}
