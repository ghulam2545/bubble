package com.ghulam.bubble.dtos;

import lombok.Builder;

@Builder
public record QueryMetric(
        long queryId,
        String query,
        long calls,
        double totalExecTime,
        double meanExecTime,
        double minExecTime,
        double maxExecTime,
        double stddevExecTime,
        long rows,
        long sharedBlksHit,
        long sharedBlksRead,
        long sharedBlksDirtied,
        long sharedBlksWritten,
        long localBlksHit,
        long localBlksRead,
        long tempBlksRead,
        long tempBlksWritten,
        double blkReadTime,
        double blkWriteTime,
        long walBytes) {
}