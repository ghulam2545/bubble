package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.DatabaseStorageInfo;
import com.ghulam.bubble.dtos.RelationStorageInfo;
import com.ghulam.bubble.dtos.SchemaStorageInfo;
import com.ghulam.bubble.repo.StorageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageRepository storageRepository;

    public DatabaseStorageInfo getDatabaseStorage() {
        return storageRepository.getDatabaseStorage();
    }

    public List<SchemaStorageInfo> getSchemaStorage() {
        return storageRepository.getSchemaStorage();
    }

    public List<RelationStorageInfo> getTableStorage(String schema, int limit) {
        return storageRepository.getTableStorage(schema, limit);
    }

    public List<RelationStorageInfo> getIndexStorage(String schema, int limit) {
        return storageRepository.getIndexStorage(schema, limit);
    }

    public List<RelationStorageInfo> getLargestRelations(int limit) {
        return storageRepository.getLargestRelations(limit);
    }
}