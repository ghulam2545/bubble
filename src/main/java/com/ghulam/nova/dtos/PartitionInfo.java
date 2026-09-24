package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record PartitionInfo(
        String parentSchema,
        String parentTable,
        String partitionName,
        String partitionStrategy,
        String partitionKey,
        String partitionBound,
        long rows,
        String sizePretty) {
}