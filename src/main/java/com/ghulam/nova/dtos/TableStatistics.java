package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record TableStatistics(
        String schema,
        String table,
        long seqScan,
        long seqTupRead,
        long idxScan,
        long idxTupFetch,
        long nTupIns,
        long nTupUpd,
        long nTupDel,
        long nTupHotUpd,
        long nLiveTup,
        long nDeadTup,
        long vacuumCount,
        long autovacuumCount,
        long analyzeCount,
        long autoanalyzeCount,
        String lastVacuum,
        String lastAutovacuum,
        String lastAnalyze,
        String lastAutoanalyze) {
}