package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.DatabaseStatistics;
import com.ghulam.nova.dtos.TableStatistics;
import com.ghulam.nova.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/tables")
    public List<TableStatistics> getTableStatistics(
            @RequestParam(required = false) String schema,
            @RequestParam(defaultValue = "50") int limit) {
        return statisticsService.getTableStatistics(schema, limit);
    }

    @GetMapping("/tables/{schema}/{table}")
    public ResponseEntity<TableStatistics> getTableStatisticsByTable(
            @PathVariable String schema,
            @PathVariable String table) {
        return statisticsService.getTableStatistics(schema, table)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/database")
    public DatabaseStatistics getDatabaseStatistics() {
        return statisticsService.getDatabaseStatistics();
    }
}