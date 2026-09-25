package com.ghulam.nova.dtos;

import lombok.Builder;

@Builder
public record VacuumProgress(
        long pid,
        String datname,
        String relname,
        String phase,
        long heapBlksTotal,
        long heapBlksScanned,
        long heapBlksVacuumed,
        long indexVacuumCount,
        long maxDeadTuples,
        long numDeadTuples) {
}