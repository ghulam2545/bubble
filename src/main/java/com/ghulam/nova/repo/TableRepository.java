package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.ColumnMetadata;
import com.ghulam.nova.dtos.TableMetadata;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TableRepository {

    private final DatabaseClient client;

    public TableRepository(DatabaseClient client) {
        this.client = client;
    }

    public List<TableMetadata> findAll(String schema, int limit, int offset) {
        String sql = """
                SELECT n.nspname AS schema, c.relname AS table,
                       pg_catalog.pg_get_userbyid(c.relowner) AS owner,
                       c.relkind AS relkind, c.relpersistence AS persistence,
                       c.reltuples::bigint AS rows_estimate,
                       pg_size_pretty(pg_relation_size(c.oid)) AS table_size,
                       pg_size_pretty(pg_indexes_size(c.oid)) AS index_size,
                       pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size,
                       c.relhasindex AS has_indexes, c.relhastriggers AS has_triggers,
                       c.relispartition AS is_partition
                FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE (:schema IS NULL OR n.nspname = :schema)
                  AND c.relkind IN ('r', 'p')
                ORDER BY n.nspname, c.relname
                LIMIT :limit OFFSET :offset
                """;

        return client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("limit", limit)
                        .addValue("offset", offset),
                (rs, rowNum) -> TableMetadata.builder()
                        .schema(rs.getString("schema"))
                        .table(rs.getString("table"))
                        .owner(rs.getString("owner"))
                        .relkind(rs.getString("relkind"))
                        .persistence(rs.getString("persistence"))
                        .rowsEstimate(rs.getLong("rows_estimate"))
                        .tableSize(rs.getString("table_size"))
                        .indexSize(rs.getString("index_size"))
                        .totalSize(rs.getString("total_size"))
                        .hasIndexes(rs.getBoolean("has_indexes"))
                        .hasTriggers(rs.getBoolean("has_triggers"))
                        .isPartition(rs.getBoolean("is_partition"))
                        .isPartitioned("p".equals(rs.getString("relkind")))
                        .build()
        );
    }

    public long countAll(String schema) {
        String sql = """
                SELECT count(*)
                FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE (:schema IS NULL OR n.nspname = :schema)
                  AND c.relkind IN ('r', 'p')
                """;
        Long count = client.queryForObject(sql, new MapSqlParameterSource("schema", schema), Long.class);
        return count != null ? count : 0L;
    }

    public Optional<TableMetadata> findBySchemaAndTable(String schema, String table) {
        String sql = """
                SELECT n.nspname AS schema, c.relname AS table,
                       pg_catalog.pg_get_userbyid(c.relowner) AS owner,
                       c.relkind AS relkind, c.relpersistence AS persistence,
                       c.reltuples::bigint AS rows_estimate,
                       pg_size_pretty(pg_relation_size(c.oid)) AS table_size,
                       pg_size_pretty(pg_indexes_size(c.oid)) AS index_size,
                       pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size,
                       c.relhasindex AS has_indexes, c.relhastriggers AS has_triggers,
                       c.relispartition AS is_partition
                FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = :schema AND c.relname = :table
                  AND c.relkind IN ('r', 'p')
                """;

        List<TableMetadata> results = client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("table", table),
                (rs, rowNum) -> TableMetadata.builder()
                        .schema(rs.getString("schema"))
                        .table(rs.getString("table"))
                        .owner(rs.getString("owner"))
                        .relkind(rs.getString("relkind"))
                        .persistence(rs.getString("persistence"))
                        .rowsEstimate(rs.getLong("rows_estimate"))
                        .tableSize(rs.getString("table_size"))
                        .indexSize(rs.getString("index_size"))
                        .totalSize(rs.getString("total_size"))
                        .hasIndexes(rs.getBoolean("has_indexes"))
                        .hasTriggers(rs.getBoolean("has_triggers"))
                        .isPartition(rs.getBoolean("is_partition"))
                        .isPartitioned("p".equals(rs.getString("relkind")))
                        .build()
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<ColumnMetadata> findColumns(String schema, String table) {
        String sql = """
                SELECT column_name, ordinal_position, data_type, udt_name,
                       is_nullable = 'YES' as nullable, column_default,
                       is_identity, is_generated, collation_name
                FROM information_schema.columns
                WHERE table_schema = :schema AND table_name = :table
                ORDER BY ordinal_position
                """;

        return client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("table", table),
                (rs, rowNum) -> ColumnMetadata.builder()
                        .columnName(rs.getString("column_name"))
                        .ordinalPosition(rs.getInt("ordinal_position"))
                        .dataType(rs.getString("data_type"))
                        .udtName(rs.getString("udt_name"))
                        .nullable(rs.getBoolean("nullable"))
                        .defaultValue(rs.getString("column_default"))
                        .identity(rs.getString("is_identity"))
                        .generated(rs.getString("is_generated"))
                        .collation(rs.getString("collation_name"))
                        .build()
        );
    }

    public Optional<ColumnMetadata> findColumn(String schema, String table, String column) {
        String sql = """
                SELECT column_name, ordinal_position, data_type, udt_name,
                       is_nullable = 'YES' as nullable, column_default,
                       is_identity, is_generated, collation_name
                FROM information_schema.columns
                WHERE table_schema = :schema AND table_name = :table AND column_name = :column
                """;

        List<ColumnMetadata> results = client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("table", table)
                        .addValue("column", column),
                (rs, rowNum) -> ColumnMetadata.builder()
                        .columnName(rs.getString("column_name"))
                        .ordinalPosition(rs.getInt("ordinal_position"))
                        .dataType(rs.getString("data_type"))
                        .udtName(rs.getString("udt_name"))
                        .nullable(rs.getBoolean("nullable"))
                        .defaultValue(rs.getString("column_default"))
                        .identity(rs.getString("is_identity"))
                        .generated(rs.getString("is_generated"))
                        .collation(rs.getString("collation_name"))
                        .build()
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Map<String, Object> getTableSize(String schema, String table) {
        String sql = """
                SELECT pg_size_pretty(pg_relation_size(c.oid)) AS table_size,
                       pg_size_pretty(pg_indexes_size(c.oid)) AS index_size,
                       pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size
                FROM pg_catalog.pg_class c
                JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = :schema AND c.relname = :table
                """;

        List<Map<String, Object>> result = client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("table", table),
                (rs, rowNum) -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("table_size", rs.getString("table_size"));
                    map.put("index_size", rs.getString("index_size"));
                    map.put("total_size", rs.getString("total_size"));
                    return map;
                }
        );
        return result.isEmpty() ? new HashMap<>() : result.get(0);
    }
}