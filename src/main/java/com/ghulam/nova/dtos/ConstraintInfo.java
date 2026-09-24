package com.ghulam.nova.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public class ConstraintInfo {
    private String constraintName;
    private String constraintType; // PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK, EXCLUSION
    private String schema;
    private String table;
    private String definition;
    private List<String> columns;
    private String foreignSchema;
    private String foreignTable;
    private List<String> foreignColumns;
    private String checkClause;
}