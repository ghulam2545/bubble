package com.ghulam.bubble.dtos;

import lombok.Builder;

@Builder
public record SchemaInfo(
        String name,
        String owner,
        int tablesCount,
        int viewsCount,
        int sequencesCount,
        int functionsCount,
        int typesCount,
        String totalSize
) {
}
