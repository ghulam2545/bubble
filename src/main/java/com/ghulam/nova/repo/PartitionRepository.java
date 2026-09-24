package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.PartitionInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartitionRepository {

    private final DatabaseClient client;

    public PartitionRepository(DatabaseClient client) {
        this.client = client;
    }

    private String getBaseQuery() {
        return """
                    SELECT
                        nmsp_parent.nspname AS parent_schema,
                        parent.relname AS parent_table,
                        child.relname AS partition_name,
                        pg_get_partkeydef(parent.oid) AS partition_key,
                        pg_get_expr(child.relpartbound, child.oid) AS partition_bound,
                        child.reltuples::bigint AS rows,
                        pg_size_pretty(pg_relation_size(child.oid)) AS size_pretty,
                        'LIST/RANGE/HASH' AS partition_strategy
                    FROM pg_inherits
                    JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
                    JOIN pg_class child ON pg_inherits.inhrelid = child.oid
                    JOIN pg_namespace nmsp_parent ON nmsp_parent.oid = parent.relnamespace
                    JOIN pg_namespace nmsp_child ON nmsp_child.oid = child.relnamespace
                    WHERE parent.relkind = 'p'
                """;
    }

    public List<PartitionInfo> findAllPartitions() {
        return client.query(getBaseQuery(), new MapSqlParameterSource(), (rs, rowNum) -> PartitionInfo.builder()
                .parentSchema(rs.getString("parent_schema"))
                .parentTable(rs.getString("parent_table"))
                .partitionName(rs.getString("partition_name"))
                .partitionStrategy(rs.getString("partition_strategy"))
                .partitionKey(rs.getString("partition_key"))
                .partitionBound(rs.getString("partition_bound"))
                .rows(rs.getLong("rows"))
                .sizePretty(rs.getString("size_pretty"))
                .build());
    }

    public List<PartitionInfo> findPartitionsForTable(String schema, String table) {
        String sql = getBaseQuery() + " AND nmsp_parent.nspname = :schema AND parent.relname = :table";
        return client.query(sql, new MapSqlParameterSource()
                .addValue("schema", schema)
                .addValue("table", table), (rs, rowNum) -> PartitionInfo.builder()
                .parentSchema(rs.getString("parent_schema"))
                .parentTable(rs.getString("parent_table"))
                .partitionName(rs.getString("partition_name"))
                .partitionStrategy(rs.getString("partition_strategy"))
                .partitionKey(rs.getString("partition_key"))
                .partitionBound(rs.getString("partition_bound"))
                .rows(rs.getLong("rows"))
                .sizePretty(rs.getString("size_pretty"))
                .build());
    }
}