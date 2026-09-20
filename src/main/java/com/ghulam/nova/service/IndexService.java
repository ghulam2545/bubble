package com.ghulam.nova.service;

import com.ghulam.nova.dtos.IndexHealthInfo;
import com.ghulam.nova.dtos.IndexInfo;
import com.ghulam.nova.dtos.PageResponse;
import com.ghulam.nova.exception.ResourceNotFoundException;
import com.ghulam.nova.repo.IndexRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IndexService {

    private final IndexRepository indexRepository;

    public IndexService(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    public PageResponse<IndexInfo> findAll(String schema, String table, int page, int size) {
        int offset = page * size;
        List<IndexInfo> content = indexRepository.findAll(schema, table, size, offset);
        long totalElements = indexRepository.countAll(schema, table);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponse.<IndexInfo>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    public List<IndexInfo> findByTable(String schema, String table) {
        return indexRepository.findAll(schema, table, 1000, 0);
    }

    public IndexInfo findByName(String schema, String table, String indexName) {
        return indexRepository.findByName(schema, table, indexName)
                .orElseThrow(() -> new ResourceNotFoundException("Index", indexName));
    }

    public List<IndexHealthInfo> getHealth() {
        List<IndexHealthInfo> healthList = new ArrayList<>();

        List<IndexInfo> unused = indexRepository.findUnusedIndexes();
        for (IndexInfo info : unused) {
            healthList.add(IndexHealthInfo.builder()
                    .indexName(info.indexName())
                    .schema(info.schema())
                    .table(info.table())
                    .status("UNUSED")
                    .issues(List.of("Index has 0 scans"))
                    .recommendations(List.of("Consider dropping the index"))
                    .sizePretty(info.sizePretty())
                    .indexScans(info.indexScans())
                    .build());
        }

        List<IndexInfo> invalid = indexRepository.findInvalidIndexes();
        for (IndexInfo info : invalid) {
            healthList.add(IndexHealthInfo.builder()
                    .indexName(info.indexName())
                    .schema(info.schema())
                    .table(info.table())
                    .status("INVALID")
                    .issues(List.of("Index is invalid or not ready"))
                    .recommendations(List.of("Drop and recreate the index"))
                    .sizePretty(info.sizePretty())
                    .indexScans(info.indexScans())
                    .build());
        }

        return healthList;
    }

    public List<IndexInfo> findUnusedIndexes() {
        return indexRepository.findUnusedIndexes();
    }

    public List<IndexInfo> findInvalidIndexes() {
        return indexRepository.findInvalidIndexes();
    }

    public List<IndexInfo> findDuplicateIndexes() {
        return indexRepository.findDuplicateIndexes();
    }

    public List<IndexInfo> findLargestIndexes(int limit) {
        return indexRepository.findLargestIndexes(limit);
    }
}