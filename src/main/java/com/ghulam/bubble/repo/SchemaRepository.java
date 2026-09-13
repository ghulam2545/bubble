package com.ghulam.bubble.repo;

import com.ghulam.bubble.dtos.SchemaInfo;
import com.ghulam.bubble.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SchemaRepository {

    private final DatabaseClient client;

    public SchemaRepository(DatabaseClient client) {
        this.client = client;
    }

    public List<SchemaInfo> findAll(boolean includeSystem) {
        String sql = """
                SELECT n.nspname AS name,
                       pg_catalog.pg_get_userbyid(n.nspowner) AS owner,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'r') AS tables_count,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'v') AS views_count,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'S') AS sequences_count,
                       (SELECT count(*) FROM pg_catalog.pg_proc p WHERE p.pronamespace = n.oid) AS functions_count,
                       (SELECT count(*) FROM pg_catalog.pg_type t WHERE t.typnamespace = n.oid) AS types_count,
                       (SELECT pg_size_pretty(sum(pg_total_relation_size(c.oid))) FROM pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'r') AS total_size
                FROM pg_catalog.pg_namespace n
                WHERE (:includeSystem = true OR n.nspname NOT LIKE 'pg_toast%' AND n.nspname NOT IN ('pg_catalog', 'information_schema'))
                ORDER BY n.nspname
                """;

        return client.query(sql, new MapSqlParameterSource("includeSystem", includeSystem), (rs, rowNum) ->
                SchemaInfo.builder()
                        .name(rs.getString("name"))
                        .owner(rs.getString("owner"))
                        .tablesCount(rs.getInt("tables_count"))
                        .viewsCount(rs.getInt("views_count"))
                        .sequencesCount(rs.getInt("sequences_count"))
                        .functionsCount(rs.getInt("functions_count"))
                        .typesCount(rs.getInt("types_count"))
                        .totalSize(rs.getString("total_size"))
                        .build()
        );
    }

    public Optional<SchemaInfo> findByName(String schema) {
        String sql = """
                SELECT n.nspname AS name,
                       pg_catalog.pg_get_userbyid(n.nspowner) AS owner,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'r') AS tables_count,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'v') AS views_count,
                       (SELECT count(*) FROM pg_catalog.pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'S') AS sequences_count,
                       (SELECT count(*) FROM pg_catalog.pg_proc p WHERE p.pronamespace = n.oid) AS functions_count,
                       (SELECT count(*) FROM pg_catalog.pg_type t WHERE t.typnamespace = n.oid) AS types_count,
                       (SELECT pg_size_pretty(sum(pg_total_relation_size(c.oid))) FROM pg_class c WHERE c.relnamespace = n.oid AND c.relkind = 'r') AS total_size
                FROM pg_catalog.pg_namespace n
                WHERE n.nspname = :schema
                """;

        List<SchemaInfo> results = client.query(sql, new MapSqlParameterSource("schema", schema), (rs, rowNum) ->
                SchemaInfo.builder()
                        .name(rs.getString("name"))
                        .owner(rs.getString("owner"))
                        .tablesCount(rs.getInt("tables_count"))
                        .viewsCount(rs.getInt("views_count"))
                        .sequencesCount(rs.getInt("sequences_count"))
                        .functionsCount(rs.getInt("functions_count"))
                        .typesCount(rs.getInt("types_count"))
                        .totalSize(rs.getString("total_size"))
                        .build()
        );

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<String> findTables(String schema) {
        String sql = "SELECT c.relname FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = :schema AND c.relkind IN ('r', 'p') ORDER BY c.relname";
        return client.queryForList(sql, new MapSqlParameterSource("schema", schema), String.class);
    }

    public List<String> findViews(String schema) {
        String sql = "SELECT c.relname FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = :schema AND c.relkind IN ('v', 'm') ORDER BY c.relname";
        return client.queryForList(sql, new MapSqlParameterSource("schema", schema), String.class);
    }

    public List<String> findSequences(String schema) {
        String sql = "SELECT c.relname FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = :schema AND c.relkind = 'S' ORDER BY c.relname";
        return client.queryForList(sql, new MapSqlParameterSource("schema", schema), String.class);
    }

    public List<String> findFunctions(String schema) {
        String sql = "SELECT p.proname FROM pg_catalog.pg_proc p JOIN pg_catalog.pg_namespace n ON n.oid = p.pronamespace WHERE n.nspname = :schema ORDER BY p.proname";
        return client.queryForList(sql, new MapSqlParameterSource("schema", schema), String.class);
    }

    public List<String> findTypes(String schema) {
        String sql = "SELECT t.typname FROM pg_catalog.pg_type t JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = :schema AND t.typtype NOT IN ('b', 'c', 'e') AND t.typname NOT LIKE '\\_%' ORDER BY t.typname";
        return client.queryForList(sql, new MapSqlParameterSource("schema", schema), String.class);
    }
}