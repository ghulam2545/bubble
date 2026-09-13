package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.SchemaInfo;
import com.ghulam.bubble.exception.ResourceNotFoundException;
import com.ghulam.bubble.repo.SchemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaService {

    private final SchemaRepository schemaRepository;

    public List<SchemaInfo> findAll(boolean includeSystem) {
        return schemaRepository.findAll(includeSystem);
    }

    public SchemaInfo findByName(String schema) {
        return schemaRepository.findByName(schema)
                .orElseThrow(() -> new ResourceNotFoundException("Schema", schema));
    }

    public List<String> findTables(String schema) {
        return schemaRepository.findTables(schema);
    }

    public List<String> findViews(String schema) {
        return schemaRepository.findViews(schema);
    }

    public List<String> findSequences(String schema) {
        return schemaRepository.findSequences(schema);
    }

    public List<String> findFunctions(String schema) {
        return schemaRepository.findFunctions(schema);
    }

    public List<String> findTypes(String schema) {
        return schemaRepository.findTypes(schema);
    }
}