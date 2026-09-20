package com.ghulam.nova.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record IndexInfo(
        String indexName,
        String schema,
        String table,
        String indexType,
        List<String> columns,
        List<String> includedColumns,
        boolean isUnique,
        boolean isPrimary,
        boolean isPartial,
        String predicate,
        boolean isValid,
        boolean isReady,
        boolean isLive,
        long sizeBytes,
        String sizePretty,
        long indexScans,
        long tuplesRead,
        long tuplesFetched
) {
}
