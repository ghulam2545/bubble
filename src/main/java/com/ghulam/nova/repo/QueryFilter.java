package com.ghulam.nova.repo;

import lombok.Builder;

@Builder
public record QueryFilter(
        Double minDuration,
        Long minCalls,
        String sort,
        String order,
        int limit,
        int offset
) {
    public QueryFilter {
        if (limit <= 0) {
            limit = 50;
        }
        if (offset < 0) {
            offset = 0;
        }
    }
}