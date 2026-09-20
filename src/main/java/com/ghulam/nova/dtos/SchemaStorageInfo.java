package com.ghulam.nova.dtos;

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
