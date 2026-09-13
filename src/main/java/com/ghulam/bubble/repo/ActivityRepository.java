package com.ghulam.bubble.repo;

import com.ghulam.bubble.dtos.ActivitySession;
import com.ghulam.bubble.service.DatabaseClient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ActivityRepository {

    private final DatabaseClient client;

    public ActivityRepository(DatabaseClient client) {
        this.client = client;
    }

    private ActivitySession mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ActivitySession.builder()
                .pid(rs.getInt("pid"))
                .username(rs.getString("username"))
                .applicationName(rs.getString("application_name"))
                .clientAddr(rs.getString("client_addr"))
                .clientPort(rs.getObject("client_port") != null ? rs.getInt("client_port") : null)
                .backendStart(rs.getString("backend_start"))
                .xactStart(rs.getString("xact_start"))
                .queryStart(rs.getString("query_start"))
                .stateChange(rs.getString("state_change"))
                .waitEventType(rs.getString("wait_event_type"))
                .waitEvent(rs.getString("wait_event"))
                .state(rs.getString("state"))
                .backendXid(rs.getString("backend_xid"))
                .backendXmin(rs.getString("backend_xmin"))
                .query(rs.getString("query"))
                .durationSeconds(rs.getObject("duration_seconds") != null ? rs.getDouble("duration_seconds") : null)
                .build();
    }

    private String getBaseQuery() {
        return """
                SELECT pid, usename, application_name, client_addr::text, client_port,
                       backend_start::text, xact_start::text, query_start::text, state_change::text,
                       wait_event_type, wait_event, state,
                       backend_xid::text, backend_xmin::text, query,
                       EXTRACT(EPOCH FROM (now() - query_start)) as duration_seconds
                FROM pg_stat_activity
                """;
    }

    public List<ActivitySession> findAll(String state, int limit) {
        StringBuilder sql = new StringBuilder(getBaseQuery());
        MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);

        if (state != null && !state.isEmpty()) {
            sql.append(" WHERE state = :state");
            params.addValue("state", state);
        }

        sql.append(" ORDER BY query_start DESC NULLS LAST LIMIT :limit");
        return client.query(sql.toString(), params, this::mapRow);
    }

    public Optional<ActivitySession> findByPid(int pid) {
        String sql = getBaseQuery() + " WHERE pid = :pid";
        try {
            return Optional.ofNullable(client.queryForObject(sql, new MapSqlParameterSource("pid", pid), this::mapRow));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<ActivitySession> findActive() {
        String sql = getBaseQuery() + " WHERE state = 'active' AND pid <> pg_backend_pid() ORDER BY query_start DESC NULLS LAST";
        return client.query(sql, new MapSqlParameterSource(), this::mapRow);
    }

    public List<ActivitySession> findIdle() {
        String sql = getBaseQuery() + " WHERE state LIKE 'idle%' AND pid <> pg_backend_pid() ORDER BY state_change DESC NULLS LAST";
        return client.query(sql, new MapSqlParameterSource(), this::mapRow);
    }

    public List<ActivitySession> findWaiting() {
        String sql = getBaseQuery() + " WHERE wait_event IS NOT NULL AND pid <> pg_backend_pid() ORDER BY query_start DESC NULLS LAST";
        return client.query(sql, new MapSqlParameterSource(), this::mapRow);
    }

    public List<ActivitySession> findLongRunning(double thresholdSeconds) {
        String sql = getBaseQuery() + """
                WHERE state = 'active'
                AND pid <> pg_backend_pid()
                AND EXTRACT(EPOCH FROM (now() - query_start)) > :threshold
                ORDER BY query_start DESC NULLS LAST
                """;
        return client.query(sql, new MapSqlParameterSource("threshold", thresholdSeconds), this::mapRow);
    }
}