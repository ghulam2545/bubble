package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record DatabaseStorageInfo(
        String databaseName,
        long totalSizeBytes,
        String totalSize,
        long tablesSizeBytes,
        String tablesSize,
        long indexesSizeBytes,
        String indexesSize
) {
}
