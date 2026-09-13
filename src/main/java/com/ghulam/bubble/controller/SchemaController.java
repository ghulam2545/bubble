package com.ghulam.bubble.controller;

import com.ghulam.bubble.dtos.SchemaInfo;
import com.ghulam.bubble.service.SchemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1")
public class SchemaController {

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping
    public List<SchemaInfo> getSchemas(@RequestParam(defaultValue = "false") boolean includeSystem) {
        return schemaService.findAll(includeSystem);
    }

    @GetMapping("/{schema}")
    public SchemaInfo getSchema(@PathVariable String schema) {
        return schemaService.findByName(schema);
    }

    @GetMapping("/{schema}/tables")
    public List<String> getTables(@PathVariable String schema) {
        return schemaService.findTables(schema);
    }

    @GetMapping("/{schema}/views")
    public List<String> getViews(@PathVariable String schema) {
        return schemaService.findViews(schema);
    }

    @GetMapping("/{schema}/sequences")
    public List<String> getSequences(@PathVariable String schema) {
        return schemaService.findSequences(schema);
    }

    @GetMapping("/{schema}/functions")
    public List<String> getFunctions(@PathVariable String schema) {
        return schemaService.findFunctions(schema);
    }

    @GetMapping("/{schema}/types")
    public List<String> getTypes(@PathVariable String schema) {
        return schemaService.findTypes(schema);
    }
}