package com.ghulam.nova.service;

import com.ghulam.nova.dtos.FunctionInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.repo.FunctionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FunctionService {

    private final FunctionRepository functionRepository;

    public FunctionService(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    public PageResponse<FunctionInfo> findAll(String schema, int page, int size) {
        int offset = page * size;
        List<FunctionInfo> functions = functionRepository.findAll(schema, size, offset);
        return PageResponse.<FunctionInfo>builder()
                .content(functions)
                .page(page)
                .size(size)
                // total elements would need a separate count query
                .build();
    }

    public Optional<FunctionInfo> findBySchemaAndName(String schema, String name) {
        return functionRepository.findBySchemaAndName(schema, name);
    }

    public String getFunctionDefinition(String schema, String name) {
        return functionRepository.getFunctionDefinition(schema, name);
    }
}