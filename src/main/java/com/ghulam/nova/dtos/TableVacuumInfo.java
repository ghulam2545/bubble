package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record TableVacuumInfo(
        String schema,
        String table,
        String lastVacuum,
        String lastAutovacuum,
        String lastAnalyze,
        String lastAutoanalyze,
        long vacuumCount,
        long autovacuumCount,
        long analyzeCount,
        long autoanalyzeCount,
        long deadTuples,
        long liveTuples) {
}