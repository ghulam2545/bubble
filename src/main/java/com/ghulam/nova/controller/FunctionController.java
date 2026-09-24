package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.FunctionInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.service.FunctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping(path = "/backend/api/v1/functions")
public class FunctionController {

    private final FunctionService functionService;

    public FunctionController(FunctionService functionService) {
        this.functionService = functionService;
    }

    @GetMapping
    public PageResponse<FunctionInfo> getFunctions(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return functionService.findAll(schema, page, size);
    }

    @GetMapping("/{schema}/{function}")
    public ResponseEntity<FunctionInfo> getFunction(
            @PathVariable String schema,
            @PathVariable String function) {
        return functionService.findBySchemaAndName(schema, function)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{schema}/{function}/definition")
    public ResponseEntity<Map<String, String>> getFunctionDefinition(
            @PathVariable String schema,
            @PathVariable String function) {
        String definition = functionService.getFunctionDefinition(schema, function);
        if (definition != null) {
            return ResponseEntity.ok(Collections.singletonMap("definition", definition));
        }
        return ResponseEntity.notFound().build();
    }
}