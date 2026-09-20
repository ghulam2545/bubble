package com.ghulam.nova.service;

import com.ghulam.nova.dtos.DatabaseStatistics;
import com.ghulam.nova.dtos.TableHealth;
import com.ghulam.nova.dtos.TableStatistics;
import com.ghulam.nova.repo.StatisticsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public StatisticsService(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    public List<TableStatistics> getTableStatistics(String schema, int limit) {
        return statisticsRepository.findTableStatistics(schema, limit);
    }

    public Optional<TableStatistics> getTableStatistics(String schema, String table) {
        return statisticsRepository.findTableStatistics(schema, table);
    }

    public DatabaseStatistics getDatabaseStatistics() {
        return statisticsRepository.getDatabaseStatistics();
    }

    public List<TableHealth> getTableHealth(String schema) {
        return statisticsRepository.findTableHealth(schema);
    }
}