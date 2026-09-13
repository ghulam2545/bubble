package com.ghulam.bubble.controller;

import com.ghulam.bubble.dtos.PageResponse;
import com.ghulam.bubble.dtos.QueryMetric;
import com.ghulam.bubble.repo.QueryFilter;
import com.ghulam.bubble.service.QueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/queries")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public PageResponse<QueryMetric> getQueries(
            @RequestParam(required = false) Double minDuration,
            @RequestParam(required = false) Long minCalls,
            @RequestParam(defaultValue = "total_exec_time") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        
        QueryFilter filter = QueryFilter.builder()
                .minDuration(minDuration)
                .minCalls(minCalls)
                .sort(sort)
                .order(order)
                .limit(limit)
                .offset(offset)
                .build();
                
        return queryService.findQueries(filter);
    }

    @GetMapping("/top")
    public List<QueryMetric> getTopQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findTopQueries(limit);
    }

    @GetMapping("/slow")
    public List<QueryMetric> getSlowQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findSlowQueries(limit);
    }

    @GetMapping("/calls")
    public List<QueryMetric> getMostFrequentQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findMostFrequentQueries(limit);
    }

    @GetMapping("/io")
    public List<QueryMetric> getTopIoQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findTopIoQueries(limit);
    }

    @GetMapping("/cpu")
    public List<QueryMetric> getTopCpuQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findTopCpuQueries(limit);
    }

    @GetMapping("/temp")
    public List<QueryMetric> getTopTempQueries(@RequestParam(defaultValue = "10") int limit) {
        return queryService.findTopTempQueries(limit);
    }

    @GetMapping("/{queryId}")
    public QueryMetric getQuery(@PathVariable long queryId) {
        return queryService.findById(queryId);
    }
}