package com.ghulam.nova.service;

import com.ghulam.nova.dtos.ColumnMetadata;
import com.ghulam.nova.dtos.ConstraintInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.dtos.TableMetadata;
import com.ghulam.nova.exception.ResourceNotFoundException;
import com.ghulam.nova.repo.ConstraintRepository;
import com.ghulam.nova.repo.TableRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TableService {

    private final TableRepository tableRepository;
    private final ConstraintRepository constraintRepository;

    public TableService(TableRepository tableRepository, ConstraintRepository constraintRepository) {
        this.tableRepository = tableRepository;
        this.constraintRepository = constraintRepository;
    }

    public PageResponse<TableMetadata> findAll(String schema, int page, int size) {
        int offset = page * size;
        List<TableMetadata> content = tableRepository.findAll(schema, size, offset);
        long totalElements = tableRepository.countAll(schema);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<TableMetadata>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    public TableMetadata findBySchemaAndTable(String schema, String table) {
        return tableRepository.findBySchemaAndTable(schema, table)
                .orElseThrow(() -> new ResourceNotFoundException("Table", schema + "." + table));
    }

    public List<ColumnMetadata> findColumns(String schema, String table) {
        return tableRepository.findColumns(schema, table);
    }

    public ColumnMetadata findColumn(String schema, String table, String column) {
        return tableRepository.findColumn(schema, table, column)
                .orElseThrow(() -> new ResourceNotFoundException("Column", schema + "." + table + "." + column));
    }

    public Map<String, Object> getTableSize(String schema, String table) {
        return tableRepository.getTableSize(schema, table);
    }

    public List<ConstraintInfo> findConstraints(String schema, String table) {
        return constraintRepository.findConstraints(schema, table);
    }

    public ConstraintInfo findPrimaryKey(String schema, String table) {
        return constraintRepository.findPrimaryKey(schema, table)
                .orElseThrow(() -> new ResourceNotFoundException("Primary Key", schema + "." + table));
    }

    public List<ConstraintInfo> findForeignKeys(String schema, String table) {
        return constraintRepository.findForeignKeys(schema, table);
    }

    public List<ConstraintInfo> findUniqueConstraints(String schema, String table) {
        return constraintRepository.findUniqueConstraints(schema, table);
    }

    public List<ConstraintInfo> findCheckConstraints(String schema, String table) {
        return constraintRepository.findCheckConstraints(schema, table);
    }
}