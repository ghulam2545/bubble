package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record RelationStorageInfo(
        String schema,
        String relationName,
        String relationType, // TABLE, INDEX, TOAST, MATERIALIZED VIEW
        long sizeBytes,
        String sizePretty,
        Long tableSizeBytes,
        String tableSizePretty,
        Long indexSizeBytes,
        String indexSizePretty,
        long totalSizeBytes,
        String totalSizePretty
) {
}
