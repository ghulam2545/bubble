package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.ExtensionInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class ExtensionRepository {

    private final DatabaseClient client;

    public ExtensionRepository(DatabaseClient client) {
        this.client = client;
    }

    private ExtensionInfo mapInstalledRow(ResultSet rs, int rowNum) throws SQLException {
        return ExtensionInfo.builder()
                .name(rs.getString("extname"))
                .version(rs.getString("extversion"))
                .schema(rs.getString("nspname"))
                .relocatable(rs.getBoolean("extrelocatable"))
                .installed(true)
                .build();
    }

    private ExtensionInfo mapAvailableRow(ResultSet rs, int rowNum) throws SQLException {
        return ExtensionInfo.builder()
                .name(rs.getString("name"))
                .defaultVersion(rs.getString("default_version"))
                .version(rs.getString("installed_version"))
                .comment(rs.getString("comment"))
                .installed(rs.getString("installed_version") != null)
                .build();
    }

    public List<ExtensionInfo> findInstalled() {
        String sql = """
            SELECT e.extname, e.extversion, n.nspname, e.extrelocatable
            FROM pg_extension e
            JOIN pg_namespace n ON n.oid = e.extnamespace
            ORDER BY e.extname
            """;
        return client.query(sql, Collections.emptyMap(), this::mapInstalledRow);
    }

    public List<ExtensionInfo> findAvailable() {
        String sql = """
            SELECT name, default_version, installed_version, comment
            FROM pg_available_extensions
            ORDER BY name
            """;
        return client.query(sql, Collections.emptyMap(), this::mapAvailableRow);
    }

    public Optional<ExtensionInfo> findByName(String name) {
        String sql = """
            SELECT name, default_version, installed_version, comment
            FROM pg_available_extensions
            WHERE name = :name
            LIMIT 1
            """;
            
        try {
            return Optional.ofNullable(client.queryForObject(
                sql, 
                new MapSqlParameterSource("name", name),
                this::mapAvailableRow
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}