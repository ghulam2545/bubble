package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public class TableMetadata {
    private String schema;
    private String table;
    private String owner;
    private String relkind;
    private String persistence;
    private long rowsEstimate;
    private String tableSize;
    private String indexSize;
    private String totalSize;
    private boolean hasIndexes;
    private boolean hasTriggers;
    private boolean isPartitioned;
    private boolean isPartition;
}