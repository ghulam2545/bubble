package com.ghulam.bubble.repo;

import com.ghulam.bubble.dtos.QueryMetric;
import com.ghulam.bubble.service.DatabaseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class QueryRepository {

    private final DatabaseClient client;

    public QueryRepository(DatabaseClient client) {
        this.client = client;
    }

    private final RowMapper<QueryMetric> queryMetricRowMapper = (rs, rowNum) -> QueryMetric.builder()
            .queryId(rs.getLong("queryid"))
            .query(rs.getString("query"))
            .calls(rs.getLong("calls"))
            .totalExecTime(rs.getDouble("total_exec_time"))
            .meanExecTime(rs.getDouble("mean_exec_time"))
            .minExecTime(rs.getDouble("min_exec_time"))
            .maxExecTime(rs.getDouble("max_exec_time"))
            .stddevExecTime(rs.getDouble("stddev_exec_time"))
            .rows(rs.getLong("rows"))
            .sharedBlksHit(rs.getLong("shared_blks_hit"))
            .sharedBlksRead(rs.getLong("shared_blks_read"))
            .sharedBlksDirtied(rs.getLong("shared_blks_dirtied"))
            .sharedBlksWritten(rs.getLong("shared_blks_written"))
            .localBlksHit(rs.getLong("local_blks_hit"))
            .localBlksRead(rs.getLong("local_blks_read"))
            .tempBlksRead(rs.getLong("temp_blks_read"))
            .tempBlksWritten(rs.getLong("temp_blks_written"))
            .blkReadTime(rs.getDouble("blk_read_time"))
            .blkWriteTime(rs.getDouble("blk_write_time"))
            .walBytes(rs.getLong("wal_bytes"))
            .build();


    public boolean isPgStatStatementsAvailable() {
        String sql = "SELECT 1 FROM pg_extension WHERE extname = 'pg_stat_statements'";
        try {
            List<Integer> results = client.queryForList(sql, new MapSqlParameterSource(), Integer.class);
            return !results.isEmpty();
        } catch (DataAccessException e) {
            log.error("Failed to check pg_stat_statements availability", e);
            return false;
        }
    }

    private boolean checkExtension() {
        if (!isPgStatStatementsAvailable()) {
            log.warn("pg_stat_statements extension is not available.");
            return false;
        }
        return true;
    }

    public List<QueryMetric> findQueries(QueryFilter filter) {
        if (!checkExtension()) return Collections.emptyList();

        StringBuilder sql = new StringBuilder("SELECT * FROM pg_stat_statements WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter.minDuration() != null) {
            sql.append(" AND total_exec_time >= :minDuration");
            params.addValue("minDuration", filter.minDuration());
        }
        if (filter.minCalls() != null) {
            sql.append(" AND calls >= :minCalls");
            params.addValue("minCalls", filter.minCalls());
        }

        String sortCol = filter.sort() != null && !filter.sort().isEmpty() ? filter.sort() : "total_exec_time";
        // Basic validation for sort column to prevent SQL injection
        if (!sortCol.matches("^[a-zA-Z0-9_]+$")) sortCol = "total_exec_time";

        String order = "asc".equalsIgnoreCase(filter.order()) ? "ASC" : "DESC";

        sql.append(" ORDER BY ").append(sortCol).append(" ").append(order);
        sql.append(" LIMIT :limit OFFSET :offset");

        params.addValue("limit", filter.limit());
        params.addValue("offset", filter.offset());

        return client.query(sql.toString(), params, queryMetricRowMapper);
    }

    public long countQueries(QueryFilter filter) {
        if (!checkExtension()) return 0;

        StringBuilder sql = new StringBuilder("SELECT count(*) FROM pg_stat_statements WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (filter.minDuration() != null) {
            sql.append(" AND total_exec_time >= :minDuration");
            params.addValue("minDuration", filter.minDuration());
        }
        if (filter.minCalls() != null) {
            sql.append(" AND calls >= :minCalls");
            params.addValue("minCalls", filter.minCalls());
        }

        Long count = client.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0L;
    }

    public Optional<QueryMetric> findById(long queryId) {
        if (!checkExtension()) return Optional.empty();

        String sql = "SELECT * FROM pg_stat_statements WHERE queryid = :queryId";
        List<QueryMetric> results = client.query(sql, new MapSqlParameterSource("queryId", queryId), queryMetricRowMapper);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<QueryMetric> findTopQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }

    public List<QueryMetric> findSlowQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY mean_exec_time DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }

    public List<QueryMetric> findMostFrequentQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY calls DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }

    public List<QueryMetric> findTopIoQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY (shared_blks_read + shared_blks_written + temp_blks_read + temp_blks_written) DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }

    public List<QueryMetric> findTopCpuQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY (total_exec_time / NULLIF(calls, 0)) DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }

    public List<QueryMetric> findTopTempQueries(int limit) {
        if (!checkExtension()) return Collections.emptyList();
        String sql = "SELECT * FROM pg_stat_statements ORDER BY (temp_blks_read + temp_blks_written) DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), queryMetricRowMapper);
    }
}