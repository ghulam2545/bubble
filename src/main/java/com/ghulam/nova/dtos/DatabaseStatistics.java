package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record DatabaseStatistics (
    String databaseName,
    int numbackends,
    long xactCommit,
    long xactRollback,
    long blksRead,
    long blksHit,
    double cacheHitRatio,
    long tupReturned,
    long tupFetched,
    long tupInserted,
    long tupUpdated,
    long tupDeleted,
    long conflicts,
    long tempFiles,
    long tempBytes,
    long deadlocks,
    double blkReadTime,
    double blkWriteTime,
    String statsReset) {
}