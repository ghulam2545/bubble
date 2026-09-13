package com.ghulam.bubble.service;

import com.ghulam.bubble.dtos.PageResponse;
import com.ghulam.bubble.dtos.QueryMetric;
import com.ghulam.bubble.exception.ResourceNotFoundException;
import com.ghulam.bubble.repo.QueryFilter;
import com.ghulam.bubble.repo.QueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {

    private final QueryRepository queryRepository;

    public QueryService(QueryRepository queryRepository) {
        this.queryRepository = queryRepository;
    }

    public PageResponse<QueryMetric> findQueries(QueryFilter filter) {
        List<QueryMetric> content = queryRepository.findQueries(filter);
        long totalElements = queryRepository.countQueries(filter);
        int totalPages = filter.limit() > 0 ? (int) Math.ceil((double) totalElements / filter.limit()) : 1;
        
        // PageResponse page uses 0-based indexing for typical page numbers, here we just pass offset/limit ratio
        int page = filter.limit() > 0 ? filter.offset() / filter.limit() : 0;

        return PageResponse.<QueryMetric>builder()
                .content(content)
                .page(page)
                .size(filter.limit())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    public QueryMetric findById(long queryId) {
        return queryRepository.findById(queryId)
                .orElseThrow(() -> new ResourceNotFoundException("Query", String.valueOf(queryId)));
    }

    public List<QueryMetric> findTopQueries(int limit) {
        return queryRepository.findTopQueries(limit);
    }

    public List<QueryMetric> findSlowQueries(int limit) {
        return queryRepository.findSlowQueries(limit);
    }

    public List<QueryMetric> findMostFrequentQueries(int limit) {
        return queryRepository.findMostFrequentQueries(limit);
    }

    public List<QueryMetric> findTopIoQueries(int limit) {
        return queryRepository.findTopIoQueries(limit);
    }

    public List<QueryMetric> findTopCpuQueries(int limit) {
        return queryRepository.findTopCpuQueries(limit);
    }

    public List<QueryMetric> findTopTempQueries(int limit) {
        return queryRepository.findTopTempQueries(limit);
    }
}