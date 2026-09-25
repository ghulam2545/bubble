package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.TableVacuumInfo;
import com.ghulam.nova.dtos.VacuumProgress;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VacuumRepository {

    private final DatabaseClient client;

    public VacuumRepository(DatabaseClient client) {
        this.client = client;
    }

    public List<TableVacuumInfo> findTableVacuumStats(String schema) {
        String sql = """
                    SELECT
                        schemaname AS schema,
                        relname AS table_name,
                        last_vacuum::text,
                        last_autovacuum::text,
                        last_analyze::text,
                        last_autoanalyze::text,
                        vacuum_count,
                        autovacuum_count,
                        analyze_count,
                        autoanalyze_count,
                        n_dead_tup AS dead_tuples,
                        n_live_tup AS live_tuples
                    FROM pg_stat_user_tables
                    WHERE (:schema IS NULL OR schemaname = :schema)
                    ORDER BY n_dead_tup DESC
                """;
        return client.query(sql, new MapSqlParameterSource("schema", schema), (rs, rowNum) -> TableVacuumInfo.builder()
                .schema(rs.getString("schema"))
                .table(rs.getString("table_name"))
                .lastVacuum(rs.getString("last_vacuum"))
                .lastAutovacuum(rs.getString("last_autovacuum"))
                .lastAnalyze(rs.getString("last_analyze"))
                .lastAutoanalyze(rs.getString("last_autoanalyze"))
                .vacuumCount(rs.getLong("vacuum_count"))
                .autovacuumCount(rs.getLong("autovacuum_count"))
                .analyzeCount(rs.getLong("analyze_count"))
                .autoanalyzeCount(rs.getLong("autoanalyze_count"))
                .deadTuples(rs.getLong("dead_tuples"))
                .liveTuples(rs.getLong("live_tuples"))
                .build());
    }

    public List<VacuumProgress> findVacuumProgress() {
        String sql = """
                    SELECT
                        pid, datname, relname, phase,
                        heap_blks_total, heap_blks_scanned, heap_blks_vacuumed,
                        index_vacuum_count, max_dead_tuples, num_dead_tuples
                    FROM pg_stat_progress_vacuum
                """;
        return client.query(sql, new MapSqlParameterSource(), (rs, rowNum) -> VacuumProgress.builder()
                .pid(rs.getLong("pid"))
                .datname(rs.getString("datname"))
                .relname(rs.getString("relname"))
                .phase(rs.getString("phase"))
                .heapBlksTotal(rs.getLong("heap_blks_total"))
                .heapBlksScanned(rs.getLong("heap_blks_scanned"))
                .heapBlksVacuumed(rs.getLong("heap_blks_vacuumed"))
                .indexVacuumCount(rs.getLong("index_vacuum_count"))
                .maxDeadTuples(rs.getLong("max_dead_tuples"))
                .numDeadTuples(rs.getLong("num_dead_tuples"))
                .build());
    }

    public void vacuum(String schema, String table, boolean analyze, boolean verbose) {
        if (!schema.matches("^[a-zA-Z0-9_]+$") || !table.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema or table name");
        }

        StringBuilder sql = new StringBuilder("VACUUM");
        if (verbose || analyze) {
            sql.append(" (");
            if (verbose) sql.append("VERBOSE");
            if (verbose && analyze) sql.append(", ");
            if (analyze) sql.append("ANALYZE");
            sql.append(")");
        }
        sql.append(String.format(" \"%s\".\"%s\"", schema, table));

        client.execute(sql.toString());
    }

    public void analyze(String schema, String table) {
        if (!schema.matches("^[a-zA-Z0-9_]+$") || !table.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema or table name");
        }
        String sql = String.format("ANALYZE \"%s\".\"%s\"", schema, table);
        client.execute(sql);
    }
}