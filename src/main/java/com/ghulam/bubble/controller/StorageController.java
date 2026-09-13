package com.ghulam.bubble.controller;

import com.ghulam.bubble.dtos.DatabaseStorageInfo;
import com.ghulam.bubble.dtos.RelationStorageInfo;
import com.ghulam.bubble.dtos.SchemaStorageInfo;
import com.ghulam.bubble.service.StorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/database")
    public DatabaseStorageInfo getDatabaseStorage() {
        return storageService.getDatabaseStorage();
    }

    @GetMapping("/schemas")
    public List<SchemaStorageInfo> getSchemaStorage() {
        return storageService.getSchemaStorage();
    }

    @GetMapping("/tables")
    public List<RelationStorageInfo> getTableStorage(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "50") int limit) {
        return storageService.getTableStorage(schema, limit);
    }

    @GetMapping("/indexes")
    public List<RelationStorageInfo> getIndexStorage(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "50") int limit) {
        return storageService.getIndexStorage(schema, limit);
    }

    @GetMapping("/largest")
    public List<RelationStorageInfo> getLargestRelations(
            @RequestParam(defaultValue = "20") int limit) {
        return storageService.getLargestRelations(limit);
    }
}
