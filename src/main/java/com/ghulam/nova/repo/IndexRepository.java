package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.IndexInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class IndexRepository {

    private final DatabaseClient client;

    public IndexRepository(DatabaseClient client) {
        this.client = client;
    }

    private IndexInfo mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return IndexInfo.builder()
                .indexName(rs.getString("index_name"))
                .schema(rs.getString("schema"))
                .table(rs.getString("table_name"))
                .indexType(rs.getString("index_type"))
                .columns(rs.getString("columns") != null ? Arrays.asList(rs.getString("columns").split(",")) : null)
                .includedColumns(rs.getString("included_columns") != null ? Arrays.asList(rs.getString("included_columns").split(",")) : null)
                .isUnique(rs.getBoolean("is_unique"))
                .isPrimary(rs.getBoolean("is_primary"))
                .isPartial(rs.getBoolean("is_partial"))
                .predicate(rs.getString("predicate"))
                .isValid(rs.getBoolean("is_valid"))
                .isReady(rs.getBoolean("is_ready"))
                .isLive(rs.getBoolean("is_live"))
                .sizeBytes(rs.getLong("size_bytes"))
                .sizePretty(rs.getString("size_pretty"))
                .indexScans(rs.getLong("index_scans"))
                .tuplesRead(rs.getLong("tuples_read"))
                .tuplesFetched(rs.getLong("tuples_fetched"))
                .build();
    }

    private String getBaseQuery() {
        return """
                    SELECT
                        i.relname AS index_name,
                        n.nspname AS schema,
                        t.relname AS table_name,
                        am.amname AS index_type,
                        array_to_string(array(SELECT a.attname FROM pg_attribute a WHERE a.attrelid = t.oid AND a.attnum = ANY(ix.indkey)), ',') AS columns,
                        NULL AS included_columns,
                        ix.indisunique AS is_unique,
                        ix.indisprimary AS is_primary,
                        (ix.indpred IS NOT NULL) AS is_partial,
                        pg_get_expr(ix.indpred, ix.indrelid) AS predicate,
                        ix.indisvalid AS is_valid,
                        ix.indisready AS is_ready,
                        ix.indislive AS is_live,
                        pg_relation_size(i.oid) AS size_bytes,
                        pg_size_pretty(pg_relation_size(i.oid)) AS size_pretty,
                        COALESCE(s.idx_scan, 0) AS index_scans,
                        COALESCE(s.idx_tup_read, 0) AS tuples_read,
                        COALESCE(s.idx_tup_fetch, 0) AS tuples_fetched
                    FROM pg_index ix
                    JOIN pg_class i ON i.oid = ix.indexrelid
                    JOIN pg_class t ON t.oid = ix.indrelid
                    JOIN pg_namespace n ON n.oid = t.relnamespace
                    JOIN pg_am am ON i.relam = am.oid
                    LEFT JOIN pg_stat_user_indexes s ON s.indexrelid = ix.indexrelid
                    WHERE n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                """;
    }

    public List<IndexInfo> findAll(String schema, String table, int limit, int offset) {
        StringBuilder sql = new StringBuilder(getBaseQuery());
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (schema != null) {
            sql.append(" AND n.nspname = :schema ");
            params.addValue("schema", schema);
        }
        if (table != null) {
            sql.append(" AND t.relname = :table ");
            params.addValue("table", table);
        }
        sql.append(" ORDER BY n.nspname, t.relname, i.relname LIMIT :limit OFFSET :offset");
        params.addValue("limit", limit).addValue("offset", offset);
        return client.query(sql.toString(), params, this::mapRow);
    }

    public long countAll(String schema, String table) {
        StringBuilder sql = new StringBuilder("""
                    SELECT count(*)
                    FROM pg_index ix
                    JOIN pg_class i ON i.oid = ix.indexrelid
                    JOIN pg_class t ON t.oid = ix.indrelid
                    JOIN pg_namespace n ON n.oid = t.relnamespace
                    WHERE n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (schema != null) {
            sql.append(" AND n.nspname = :schema ");
            params.addValue("schema", schema);
        }
        if (table != null) {
            sql.append(" AND t.relname = :table ");
            params.addValue("table", table);
        }
        Long count = client.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count : 0;
    }

    public Optional<IndexInfo> findByName(String schema, String table, String indexName) {
        String sql = getBaseQuery() + " AND n.nspname = :schema AND t.relname = :table AND i.relname = :indexName";
        List<IndexInfo> results = client.query(sql, new MapSqlParameterSource()
                .addValue("schema", schema)
                .addValue("table", table)
                .addValue("indexName", indexName), this::mapRow);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<IndexInfo> findUnusedIndexes() {
        String sql = getBaseQuery() + " AND s.idx_scan = 0 AND NOT ix.indisprimary AND NOT ix.indisunique ORDER BY pg_relation_size(i.oid) DESC";
        return client.query(sql, new MapSqlParameterSource(), this::mapRow);
    }

    public List<IndexInfo> findInvalidIndexes() {
        String sql = getBaseQuery() + " AND (ix.indisvalid = false OR ix.indisready = false)";
        return client.query(sql, new MapSqlParameterSource(), this::mapRow);
    }

    public List<IndexInfo> findDuplicateIndexes() {
        String sql = """
                    WITH index_defs AS (
                        SELECT i.relname AS index_name, n.nspname AS schema, t.relname AS table_name,
                               pg_get_indexdef(i.oid) as def, ix.indexrelid, ix.indrelid
                        FROM pg_index ix
                        JOIN pg_class i ON i.oid = ix.indexrelid
                        JOIN pg_class t ON t.oid = ix.indrelid
                        JOIN pg_namespace n ON n.oid = t.relnamespace
                        WHERE n.nspname NOT IN ('pg_catalog', 'information_schema', 'pg_toast')
                    ),
                    duplicates AS (
                        SELECT def, table_name, schema, count(*)
                        FROM index_defs
                        GROUP BY def, table_name, schema
                        HAVING count(*) > 1
                    )
                    SELECT d.def FROM duplicates d
                """;
        return List.of();
    }

    public List<IndexInfo> findLargestIndexes(int limit) {
        String sql = getBaseQuery() + " ORDER BY pg_relation_size(i.oid) DESC LIMIT :limit";
        return client.query(sql, new MapSqlParameterSource("limit", limit), this::mapRow);
    }
}