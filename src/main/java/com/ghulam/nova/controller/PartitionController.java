package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.PartitionInfo;
import com.ghulam.nova.service.PartitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/partitions")
public class PartitionController {

    private final PartitionService partitionService;

    public PartitionController(PartitionService partitionService) {
        this.partitionService = partitionService;
    }

    @GetMapping
    public List<PartitionInfo> getPartitions() {
        return partitionService.findAllPartitions();
    }

    @GetMapping("/{schema}/{table}")
    public List<PartitionInfo> getPartitionsForTable(@PathVariable String schema, @PathVariable String table) {
        return partitionService.findPartitionsForTable(schema, table);
    }
}