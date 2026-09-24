package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.FunctionInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class FunctionRepository {

    private final DatabaseClient client;

    public FunctionRepository(DatabaseClient client) {
        this.client = client;
    }

    private FunctionInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FunctionInfo.builder()
                .schema(rs.getString("schema_name"))
                .name(rs.getString("function_name"))
                .arguments(rs.getString("arguments"))
                .returnType(rs.getString("return_type"))
                .language(rs.getString("language_name"))
                .volatility(rs.getString("volatility"))
                .parallelSafety(rs.getString("parallel_safety"))
                .securityDefiner(rs.getBoolean("security_definer"))
                .owner(rs.getString("owner_name"))
                .definition(rs.getString("definition"))
                .build();
    }

    public List<FunctionInfo> findAll(String schema, int limit, int offset) {
        StringBuilder sql = new StringBuilder("""
                SELECT n.nspname as schema_name, p.proname as function_name,
                       pg_get_function_arguments(p.oid) as arguments,
                       pg_get_function_result(p.oid) as return_type,
                       l.lanname as language_name,
                       p.provolatile as volatility,
                       p.proparallel as parallel_safety,
                       p.prosecdef as security_definer,
                       pg_get_userbyid(p.proowner) as owner_name,
                       pg_get_functiondef(p.oid) as definition
                FROM pg_proc p
                JOIN pg_namespace n ON n.oid = p.pronamespace
                JOIN pg_language l ON l.oid = p.prolang
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (schema != null && !schema.isEmpty()) {
            sql.append(" WHERE n.nspname = :schema");
            params.addValue("schema", schema);
        } else {
            sql.append(" WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')");
        }

        sql.append(" ORDER BY n.nspname, p.proname LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit);
        params.addValue("offset", offset);

        return client.query(sql.toString(), params, this::mapRow);
    }

    public Optional<FunctionInfo> findBySchemaAndName(String schema, String name) {
        String sql = """
                SELECT n.nspname as schema_name, p.proname as function_name,
                       pg_get_function_arguments(p.oid) as arguments,
                       pg_get_function_result(p.oid) as return_type,
                       l.lanname as language_name,
                       p.provolatile as volatility,
                       p.proparallel as parallel_safety,
                       p.prosecdef as security_definer,
                       pg_get_userbyid(p.proowner) as owner_name,
                       pg_get_functiondef(p.oid) as definition
                FROM pg_proc p
                JOIN pg_namespace n ON n.oid = p.pronamespace
                JOIN pg_language l ON l.oid = p.prolang
                WHERE n.nspname = :schema AND p.proname = :name
                LIMIT 1
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("schema", schema)
                .addValue("name", name);

        try {
            return Optional.ofNullable(client.queryForObject(sql, params, this::mapRow));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public String getFunctionDefinition(String schema, String name) {
        return findBySchemaAndName(schema, name).map(FunctionInfo::definition).orElse(null);
    }
}