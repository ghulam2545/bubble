package com.ghulam.nova.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record ConstraintInfo(
        String constraintName,
        String constraintType, // PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK, EXCLUSION
        String schema,
        String table,
        String definition,
        List<String> columns,
        String foreignSchema,
        String foreignTable,
        List<String> foreignColumns,
        String checkClause) {
}