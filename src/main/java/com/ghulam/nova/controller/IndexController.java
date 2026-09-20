package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.IndexHealthInfo;
import com.ghulam.nova.dtos.IndexInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.service.IndexService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/indexes")
public class IndexController {

    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @GetMapping
    public PageResponse<IndexInfo> getIndexes(
            @RequestParam(required = false) String schema,
            @RequestParam(required = false) String table,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return indexService.findAll(schema, table, page, size);
    }

    @GetMapping("/{schema}/{table}")
    public List<IndexInfo> getTableIndexes(@PathVariable String schema, @PathVariable String table) {
        return indexService.findByTable(schema, table);
    }

    @GetMapping("/{schema}/{table}/{index}")
    public IndexInfo getIndex(@PathVariable String schema, @PathVariable String table, @PathVariable String index) {
        return indexService.findByName(schema, table, index);
    }

    @GetMapping("/health")
    public List<IndexHealthInfo> getHealth() {
        return indexService.getHealth();
    }

    @GetMapping("/unused")
    public List<IndexInfo> getUnusedIndexes() {
        return indexService.findUnusedIndexes();
    }

    @GetMapping("/invalid")
    public List<IndexInfo> getInvalidIndexes() {
        return indexService.findInvalidIndexes();
    }

    @GetMapping("/duplicate")
    public List<IndexInfo> getDuplicateIndexes() {
        return indexService.findDuplicateIndexes();
    }

    @GetMapping("/largest")
    public List<IndexInfo> getLargestIndexes(@RequestParam(defaultValue = "20") int limit) {
        return indexService.findLargestIndexes(limit);
    }
}