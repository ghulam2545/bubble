package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.DatabaseStorageInfo;
import com.ghulam.nova.dtos.RelationStorageInfo;
import com.ghulam.nova.dtos.SchemaStorageInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class StorageRepository {

    private final DatabaseClient client;

    public StorageRepository(DatabaseClient client) {
        this.client = client;
    }

    public DatabaseStorageInfo getDatabaseStorage() {
        String sql = """
                SELECT
                    current_database() AS database_name,
                    pg_database_size(current_database()) AS total_size_bytes,
                    pg_size_pretty(pg_database_size(current_database())) AS total_size,
                    COALESCE(sum(pg_table_size(c.oid)), 0)::bigint AS tables_size_bytes,
                    pg_size_pretty(COALESCE(sum(pg_table_size(c.oid)), 0)::bigint) AS tables_size,
                    COALESCE(sum(pg_indexes_size(c.oid)), 0)::bigint AS indexes_size_bytes,
                    pg_size_pretty(COALESCE(sum(pg_indexes_size(c.oid)), 0)::bigint) AS indexes_size
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE c.relkind = 'r'
                  AND n.nspname NOT LIKE 'pg_toast%'
                  AND n.nspname NOT IN ('pg_catalog', 'information_schema')
                """;

        return client.queryForObject(sql, new MapSqlParameterSource(), this::mapDatabaseStorage);
    }

    public List<SchemaStorageInfo> getSchemaStorage() {
        String sql = """
                SELECT
                    n.nspname AS schema,
                    COALESCE(sum(pg_total_relation_size(c.oid)), 0)::bigint AS total_size_bytes,
                    pg_size_pretty(COALESCE(sum(pg_total_relation_size(c.oid)), 0)::bigint) AS total_size_pretty,
                    COUNT(CASE WHEN c.relkind = 'r' THEN 1 END) AS tables_count,
                    COUNT(CASE WHEN c.relkind = 'i' THEN 1 END) AS indexes_count
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname NOT LIKE 'pg_toast%'
                  AND n.nspname NOT IN ('pg_catalog', 'information_schema')
                GROUP BY n.nspname
                ORDER BY total_size_bytes DESC
                """;

        return client.query(sql, new MapSqlParameterSource(), this::mapSchemaStorage);
    }

    public List<RelationStorageInfo> getTableStorage(String schema, int limit) {
        String sql = """
                SELECT
                    n.nspname AS schema,
                    c.relname AS relation_name,
                    'TABLE' AS relation_type,
                    pg_relation_size(c.oid) AS size_bytes,
                    pg_size_pretty(pg_relation_size(c.oid)) AS size_pretty,
                    pg_table_size(c.oid) AS table_size_bytes,
                    pg_size_pretty(pg_table_size(c.oid)) AS table_size_pretty,
                    pg_indexes_size(c.oid) AS index_size_bytes,
                    pg_size_pretty(pg_indexes_size(c.oid)) AS index_size_pretty,
                    pg_total_relation_size(c.oid) AS total_size_bytes,
                    pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size_pretty
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE c.relkind = 'r'
                  AND (:schema IS NULL OR n.nspname = :schema)
                ORDER BY total_size_bytes DESC
                LIMIT :limit
                """;

        return client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("limit", limit),
                (rs, rowNum) -> RelationStorageInfo.builder()
                        .schema(rs.getString("schema"))
                        .relationName(rs.getString("relation_name"))
                        .relationType(rs.getString("relation_type"))
                        .sizeBytes(rs.getLong("size_bytes"))
                        .sizePretty(rs.getString("size_pretty"))
                        .tableSizeBytes(rs.getLong("table_size_bytes"))
                        .tableSizePretty(rs.getString("table_size_pretty"))
                        .indexSizeBytes(rs.getLong("index_size_bytes"))
                        .indexSizePretty(rs.getString("index_size_pretty"))
                        .totalSizeBytes(rs.getLong("total_size_bytes"))
                        .totalSizePretty(rs.getString("total_size_pretty"))
                        .build()
        );
    }

    public List<RelationStorageInfo> getIndexStorage(String schema, int limit) {
        String sql = """
                SELECT
                    n.nspname AS schema,
                    c.relname AS relation_name,
                    'INDEX' AS relation_type,
                    pg_relation_size(c.oid) AS size_bytes,
                    pg_size_pretty(pg_relation_size(c.oid)) AS size_pretty,
                    NULL::bigint AS table_size_bytes,
                    NULL AS table_size_pretty,
                    NULL::bigint AS index_size_bytes,
                    NULL AS index_size_pretty,
                    pg_relation_size(c.oid) AS total_size_bytes,
                    pg_size_pretty(pg_relation_size(c.oid)) AS total_size_pretty
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE c.relkind = 'i'
                  AND (:schema IS NULL OR n.nspname = :schema)
                ORDER BY total_size_bytes DESC
                LIMIT :limit
                """;

        return client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("limit", limit),
                (rs, rowNum) -> RelationStorageInfo.builder()
                        .schema(rs.getString("schema"))
                        .relationName(rs.getString("relation_name"))
                        .relationType(rs.getString("relation_type"))
                        .sizeBytes(rs.getLong("size_bytes"))
                        .sizePretty(rs.getString("size_pretty"))
                        .tableSizeBytes(rs.getObject("table_size_bytes", Long.class))
                        .tableSizePretty(rs.getString("table_size_pretty"))
                        .indexSizeBytes(rs.getObject("index_size_bytes", Long.class))
                        .indexSizePretty(rs.getString("index_size_pretty"))
                        .totalSizeBytes(rs.getLong("total_size_bytes"))
                        .totalSizePretty(rs.getString("total_size_pretty"))
                        .build()
        );
    }

    public List<RelationStorageInfo> getLargestRelations(int limit) {
        String sql = """
                SELECT
                    n.nspname AS schema,
                    c.relname AS relation_name,
                    CASE c.relkind
                        WHEN 'r' THEN 'TABLE'
                        WHEN 'i' THEN 'INDEX'
                        WHEN 't' THEN 'TOAST'
                        WHEN 'm' THEN 'MATERIALIZED VIEW'
                        ELSE c.relkind::text
                    END AS relation_type,
                    pg_relation_size(c.oid) AS size_bytes,
                    pg_size_pretty(pg_relation_size(c.oid)) AS size_pretty,
                    CASE WHEN c.relkind = 'r' THEN pg_table_size(c.oid) ELSE NULL END AS table_size_bytes,
                    CASE WHEN c.relkind = 'r' THEN pg_size_pretty(pg_table_size(c.oid)) ELSE NULL END AS table_size_pretty,
                    CASE WHEN c.relkind = 'r' THEN pg_indexes_size(c.oid) ELSE NULL END AS index_size_bytes,
                    CASE WHEN c.relkind = 'r' THEN pg_size_pretty(pg_indexes_size(c.oid)) ELSE NULL END AS index_size_pretty,
                    pg_total_relation_size(c.oid) AS total_size_bytes,
                    pg_size_pretty(pg_total_relation_size(c.oid)) AS total_size_pretty
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                ORDER BY total_size_bytes DESC
                LIMIT :limit
                """;

        return client.query(sql, new MapSqlParameterSource("limit", limit),
                (rs, rowNum) -> RelationStorageInfo.builder()
                        .schema(rs.getString("schema"))
                        .relationName(rs.getString("relation_name"))
                        .relationType(rs.getString("relation_type"))
                        .sizeBytes(rs.getLong("size_bytes"))
                        .sizePretty(rs.getString("size_pretty"))
                        .tableSizeBytes(rs.getObject("table_size_bytes", Long.class))
                        .tableSizePretty(rs.getString("table_size_pretty"))
                        .indexSizeBytes(rs.getObject("index_size_bytes", Long.class))
                        .indexSizePretty(rs.getString("index_size_pretty"))
                        .totalSizeBytes(rs.getLong("total_size_bytes"))
                        .totalSizePretty(rs.getString("total_size_pretty"))
                        .build()
        );
    }

    private SchemaStorageInfo mapSchemaStorage(ResultSet rs, int rowNum) throws SQLException {
        return SchemaStorageInfo.builder()
                .schema(rs.getString("schema"))
                .totalSizeBytes(rs.getLong("total_size_bytes"))
                .totalSizePretty(rs.getString("total_size_pretty"))
                .tablesCount(rs.getInt("tables_count"))
                .indexesCount(rs.getInt("indexes_count"))
                .build();
    }

    private DatabaseStorageInfo mapDatabaseStorage(ResultSet rs, int rowNum) throws SQLException {
        return DatabaseStorageInfo.builder()
                .databaseName(rs.getString("database_name"))
                .totalSizeBytes(rs.getLong("total_size_bytes"))
                .totalSize(rs.getString("total_size"))
                .tablesSizeBytes(rs.getLong("tables_size_bytes"))
                .tablesSize(rs.getString("tables_size"))
                .indexesSizeBytes(rs.getLong("indexes_size_bytes"))
                .indexesSize(rs.getString("indexes_size"))
                .build();
    }

}