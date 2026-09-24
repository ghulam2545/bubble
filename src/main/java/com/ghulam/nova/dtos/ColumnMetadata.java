package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record ColumnMetadata(
        String columnName,
        int ordinalPosition,
        String dataType,
        String udtName,
        boolean nullable,
        String defaultValue,
        String identity,
        String generated,
        String collation,
        String comment,
        Integer arrayDimension) {
}