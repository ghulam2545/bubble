package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record TableMetadata(
        String schema,
        String table,
        String owner,
        String relkind,
        String persistence,
        long rowsEstimate,
        String tableSize,
        String indexSize,
        String totalSize,
        boolean hasIndexes,
        boolean hasTriggers,
        boolean isPartitioned,
        boolean isPartition) {
}