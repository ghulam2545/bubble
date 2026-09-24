package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public class ColumnMetadata {
    private String columnName;
    private int ordinalPosition;
    private String dataType;
    private String udtName;
    private boolean nullable;
    private String defaultValue;
    private String identity;
    private String generated;
    private String collation;
    private String comment;
    private Integer arrayDimension;
}