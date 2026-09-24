package com.ghulam.nova.service;

import com.ghulam.nova.dtos.PartitionInfo;
import com.ghulam.nova.repo.PartitionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartitionService {

    private final PartitionRepository partitionRepository;

    public PartitionService(PartitionRepository partitionRepository) {
        this.partitionRepository = partitionRepository;
    }

    public List<PartitionInfo> findAllPartitions() {
        return partitionRepository.findAllPartitions();
    }

    public List<PartitionInfo> findPartitionsForTable(String schema, String table) {
        return partitionRepository.findPartitionsForTable(schema, table);
    }
}