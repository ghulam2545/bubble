package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.ColumnMetadata;
import com.ghulam.nova.dtos.ConstraintInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.dtos.TableMetadata;
import com.ghulam.nova.service.TableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/backend/api/v1/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public PageResponse<TableMetadata> getTables(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return tableService.findAll(schema, page, size);
    }

    @GetMapping("/{schema}/{table}")
    public TableMetadata getTable(@PathVariable String schema, @PathVariable String table) {
        return tableService.findBySchemaAndTable(schema, table);
    }

    @GetMapping("/{schema}/{table}/columns")
    public List<ColumnMetadata> getColumns(@PathVariable String schema, @PathVariable String table) {
        return tableService.findColumns(schema, table);
    }

    @GetMapping("/{schema}/{table}/columns/{column}")
    public ColumnMetadata getColumn(@PathVariable String schema, @PathVariable String table, @PathVariable String column) {
        return tableService.findColumn(schema, table, column);
    }

    @GetMapping("/{schema}/{table}/size")
    public Map<String, Object> getTableSize(@PathVariable String schema, @PathVariable String table) {
        return tableService.getTableSize(schema, table);
    }

    @GetMapping("/{schema}/{table}/constraints")
    public List<ConstraintInfo> getConstraints(@PathVariable String schema, @PathVariable String table) {
        return tableService.findConstraints(schema, table);
    }

    @GetMapping("/{schema}/{table}/constraints/primary-key")
    public ConstraintInfo getPrimaryKey(@PathVariable String schema, @PathVariable String table) {
        return tableService.findPrimaryKey(schema, table);
    }

    @GetMapping("/{schema}/{table}/constraints/foreign-keys")
    public List<ConstraintInfo> getForeignKeys(@PathVariable String schema, @PathVariable String table) {
        return tableService.findForeignKeys(schema, table);
    }

    @GetMapping("/{schema}/{table}/constraints/unique")
    public List<ConstraintInfo> getUniqueConstraints(@PathVariable String schema, @PathVariable String table) {
        return tableService.findUniqueConstraints(schema, table);
    }

    @GetMapping("/{schema}/{table}/constraints/check")
    public List<ConstraintInfo> getCheckConstraints(@PathVariable String schema, @PathVariable String table) {
        return tableService.findCheckConstraints(schema, table);
    }
}