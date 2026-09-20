package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.DatabaseStatistics;
import com.ghulam.nova.dtos.TableHealth;
import com.ghulam.nova.dtos.TableStatistics;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class StatisticsRepository {

    private final DatabaseClient client;

    public StatisticsRepository(DatabaseClient client) {
        this.client = client;
    }

    private TableStatistics mapTableStatistics(ResultSet rs, int rowNum) throws SQLException {
        return TableStatistics.builder()
                .schema(rs.getString("schemaname"))
                .table(rs.getString("relname"))
                .seqScan(rs.getLong("seq_scan"))
                .seqTupRead(rs.getLong("seq_tup_read"))
                .idxScan(rs.getLong("idx_scan"))
                .idxTupFetch(rs.getLong("idx_tup_fetch"))
                .nTupIns(rs.getLong("n_tup_ins"))
                .nTupUpd(rs.getLong("n_tup_upd"))
                .nTupDel(rs.getLong("n_tup_del"))
                .nTupHotUpd(rs.getLong("n_tup_hot_upd"))
                .nLiveTup(rs.getLong("n_live_tup"))
                .nDeadTup(rs.getLong("n_dead_tup"))
                .vacuumCount(rs.getLong("vacuum_count"))
                .autovacuumCount(rs.getLong("autovacuum_count"))
                .analyzeCount(rs.getLong("analyze_count"))
                .autoanalyzeCount(rs.getLong("autoanalyze_count"))
                .lastVacuum(rs.getString("last_vacuum"))
                .lastAutovacuum(rs.getString("last_autovacuum"))
                .lastAnalyze(rs.getString("last_analyze"))
                .lastAutoanalyze(rs.getString("last_autoanalyze"))
                .build();
    }

    public List<TableStatistics> findTableStatistics(String schema, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT schemaname, relname, seq_scan, seq_tup_read, idx_scan, idx_tup_fetch,
                       n_tup_ins, n_tup_upd, n_tup_del, n_tup_hot_upd, n_live_tup, n_dead_tup,
                       vacuum_count, autovacuum_count, analyze_count, autoanalyze_count,
                       last_vacuum::text, last_autovacuum::text, last_analyze::text, last_autoanalyze::text
                FROM pg_stat_user_tables
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (schema != null && !schema.isEmpty()) {
            sql.append(" WHERE schemaname = :schema");
            params.addValue("schema", schema);
        }

        sql.append(" ORDER BY schemaname, relname LIMIT :limit");
        params.addValue("limit", limit);

        return client.query(sql.toString(), params, this::mapTableStatistics);
    }

    public Optional<TableStatistics> findTableStatistics(String schema, String table) {
        String sql = """
                SELECT schemaname, relname, seq_scan, seq_tup_read, idx_scan, idx_tup_fetch,
                       n_tup_ins, n_tup_upd, n_tup_del, n_tup_hot_upd, n_live_tup, n_dead_tup,
                       vacuum_count, autovacuum_count, analyze_count, autoanalyze_count,
                       last_vacuum::text, last_autovacuum::text, last_analyze::text, last_autoanalyze::text
                FROM pg_stat_user_tables
                WHERE schemaname = :schema AND relname = :table
                """;
        try {
            return Optional.ofNullable(client.queryForObject(
                    sql,
                    new MapSqlParameterSource("schema", schema).addValue("table", table),
                    this::mapTableStatistics
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public DatabaseStatistics getDatabaseStatistics() {
        String sql = """
                SELECT datname, numbackends, xact_commit, xact_rollback, blks_read, blks_hit,
                       (blks_hit * 100.0) / NULLIF(blks_hit + blks_read, 0) as cache_hit_ratio,
                       tup_returned, tup_fetched, tup_inserted, tup_updated, tup_deleted,
                       conflicts, temp_files, temp_bytes, deadlocks, 
                       blk_read_time, blk_write_time, stats_reset::text
                FROM pg_stat_database 
                WHERE datname = current_database()
                """;
        return client.queryForObject(sql, Collections.emptyMap(), (rs, rowNum) -> DatabaseStatistics.builder()
                .databaseName(rs.getString("datname"))
                .numbackends(rs.getInt("numbackends"))
                .xactCommit(rs.getLong("xact_commit"))
                .xactRollback(rs.getLong("xact_rollback"))
                .blksRead(rs.getLong("blks_read"))
                .blksHit(rs.getLong("blks_hit"))
                .cacheHitRatio(rs.getDouble("cache_hit_ratio"))
                .tupReturned(rs.getLong("tup_returned"))
                .tupFetched(rs.getLong("tup_fetched"))
                .tupInserted(rs.getLong("tup_inserted"))
                .tupUpdated(rs.getLong("tup_updated"))
                .tupDeleted(rs.getLong("tup_deleted"))
                .conflicts(rs.getLong("conflicts"))
                .tempFiles(rs.getLong("temp_files"))
                .tempBytes(rs.getLong("temp_bytes"))
                .deadlocks(rs.getLong("deadlocks"))
                .blkReadTime(rs.getDouble("blk_read_time"))
                .blkWriteTime(rs.getDouble("blk_write_time"))
                .statsReset(rs.getString("stats_reset"))
                .build());
    }

    public List<TableHealth> findTableHealth(String schema) {
        StringBuilder sql = new StringBuilder("""
                SELECT schemaname, relname,
                       n_live_tup, n_dead_tup,
                       seq_scan, idx_scan,
                       CAST(n_dead_tup AS float) / NULLIF(n_live_tup + n_dead_tup, 0) as dead_tuple_ratio,
                       CAST(seq_scan AS float) / NULLIF(seq_scan + idx_scan, 0) as seq_scan_ratio,
                       CAST(idx_scan AS float) / NULLIF(seq_scan + idx_scan, 0) as index_usage_ratio
                FROM pg_stat_user_tables
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (schema != null && !schema.isEmpty()) {
            sql.append(" WHERE schemaname = :schema");
            params.addValue("schema", schema);
        }

        return client.query(sql.toString(), params, (rs, rowNum) -> {
            String sch = rs.getString("schemaname");
            String tab = rs.getString("relname");
            long nLiveTup = rs.getLong("n_live_tup");

            double deadTupleRatio = rs.getDouble("dead_tuple_ratio");
            double seqScanRatio = rs.getDouble("seq_scan_ratio");
            double indexUsageRatio = rs.getDouble("index_usage_ratio");

            String status = "HEALTHY";
            List<String> issues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            if (deadTupleRatio > 0.25) {
                status = "CRITICAL";
                issues.add("High amount of dead tuples (" + String.format("%.2f%%", deadTupleRatio * 100) + ")");
                recommendations.add("Run VACUUM FULL or VACUUM to reclaim space.");
            } else if (deadTupleRatio > 0.10) {
                status = "WARNING";
                issues.add("Moderate amount of dead tuples (" + String.format("%.2f%%", deadTupleRatio * 100) + ")");
                recommendations.add("Consider tuning autovacuum settings.");
            }

            if (nLiveTup > 10000 && seqScanRatio > 0.50) {
                if ("HEALTHY".equals(status)) {
                    status = "WARNING";
                }
                issues.add("High sequential scan ratio on large table (" + String.format("%.2f%%", seqScanRatio * 100) + ")");
                recommendations.add("Consider adding indexes to support frequent queries.");
            }

            return TableHealth.builder()
                    .schema(sch)
                    .table(tab)
                    .status(status)
                    .deadTupleRatio(deadTupleRatio)
                    .seqScanRatio(seqScanRatio)
                    .indexUsageRatio(indexUsageRatio)
                    .issues(issues)
                    .recommendations(recommendations)
                    .build();
        });
    }
}