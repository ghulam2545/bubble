package com.ghulam.nova.service;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseClient {

    private final ConnectionService connectionService;

    public DatabaseClient(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> List<T> queryForList(
            String sql,
            SqlParameterSource params,
            Class<T> type) {

        return connectionService.jdbc()
                .queryForList(sql, params, type);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> T queryForObject(
            String sql,
            SqlParameterSource params,
            RowMapper<T> rowMapper) {

        return connectionService.jdbc()
                .queryForObject(sql, params, rowMapper);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> T queryForObject(
            String sql,
            SqlParameterSource params,
            Class<T> type) {

        return connectionService.jdbc()
                .queryForObject(sql, params, type);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> T queryForObject(
            String sql,
            Map<String, ?> params,
            RowMapper<T> rowMapper) {

        return connectionService.jdbc()
                .queryForObject(sql, params, rowMapper);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> T queryForObject(
            String sql,
            Map<String, ?> params,
            Class<T> type) {

        return connectionService.jdbc()
                .queryForObject(sql, params, type);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> List<T> query(
            String sql,
            SqlParameterSource params,
            RowMapper<T> rowMapper) {

        return connectionService.jdbc()
                .query(sql, params, rowMapper);
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    public <T> List<T> query(
            String sql,
            Map<String, ?> params,
            RowMapper<T> rowMapper) {

        return connectionService.jdbc()
                .query(sql, params, rowMapper);
    }
}