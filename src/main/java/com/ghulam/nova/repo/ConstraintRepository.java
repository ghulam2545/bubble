package com.ghulam.nova.repo;

import com.ghulam.nova.dtos.ConstraintInfo;
import com.ghulam.nova.service.DatabaseClient;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class ConstraintRepository {

    private final DatabaseClient client;

    public ConstraintRepository(DatabaseClient client) {
        this.client = client;
    }

    private List<ConstraintInfo> findConstraintsByType(String schema, String table, String typeFilter) {
        String sql = """
                SELECT c.conname, c.contype,
                       pg_get_constraintdef(c.oid) AS definition,
                       array_to_string(ARRAY(SELECT a.attname FROM pg_attribute a WHERE a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)), ',') AS columns,
                       array_to_string(ARRAY(SELECT a.attname FROM pg_attribute a WHERE a.attrelid = c.confrelid AND a.attnum = ANY(c.confkey)), ',') AS foreign_columns,
                       fn.nspname AS foreign_schema,
                       fc.relname AS foreign_table
                FROM pg_constraint c
                JOIN pg_class cl ON cl.oid = c.conrelid
                JOIN pg_namespace n ON n.oid = cl.relnamespace
                LEFT JOIN pg_class fc ON fc.oid = c.confrelid
                LEFT JOIN pg_namespace fn ON fn.oid = fc.relnamespace
                WHERE n.nspname = :schema AND cl.relname = :table
                """ + (typeFilter != null ? " AND c.contype IN (" + typeFilter + ")" : "");

        return client.query(sql, new MapSqlParameterSource()
                        .addValue("schema", schema)
                        .addValue("table", table),
                (rs, rowNum) -> {
                    String contype = rs.getString("contype");
                    String constraintType = switch (contype) {
                        case "p" -> "PRIMARY KEY";
                        case "f" -> "FOREIGN KEY";
                        case "u" -> "UNIQUE";
                        case "c" -> "CHECK";
                        case "x" -> "EXCLUSION";
                        default -> contype;
                    };

                    String colsStr = rs.getString("columns");
                    List<String> columns = colsStr != null && !colsStr.isEmpty() ? Arrays.asList(colsStr.split(",")) : null;

                    String fColsStr = rs.getString("foreign_columns");
                    List<String> foreignColumns = fColsStr != null && !fColsStr.isEmpty() ? Arrays.asList(fColsStr.split(",")) : null;

                    return ConstraintInfo.builder()
                            .constraintName(rs.getString("conname"))
                            .constraintType(constraintType)
                            .schema(schema)
                            .table(table)
                            .definition(rs.getString("definition"))
                            .columns(columns)
                            .foreignSchema(rs.getString("foreign_schema"))
                            .foreignTable(rs.getString("foreign_table"))
                            .foreignColumns(foreignColumns)
                            .build();
                }
        );
    }

    public List<ConstraintInfo> findConstraints(String schema, String table) {
        return findConstraintsByType(schema, table, null);
    }

    public Optional<ConstraintInfo> findPrimaryKey(String schema, String table) {
        List<ConstraintInfo> constraints = findConstraintsByType(schema, table, "'p'");
        return constraints.isEmpty() ? Optional.empty() : Optional.of(constraints.get(0));
    }

    public List<ConstraintInfo> findForeignKeys(String schema, String table) {
        return findConstraintsByType(schema, table, "'f'");
    }

    public List<ConstraintInfo> findUniqueConstraints(String schema, String table) {
        return findConstraintsByType(schema, table, "'u'");
    }

    public List<ConstraintInfo> findCheckConstraints(String schema, String table) {
        return findConstraintsByType(schema, table, "'c'");
    }
}