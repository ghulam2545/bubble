package com.ghulam.nova.service;

import com.ghulam.nova.dtos.TableVacuumInfo;
import com.ghulam.nova.dtos.VacuumProgress;
import com.ghulam.nova.repo.VacuumRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VacuumService {

    private final VacuumRepository vacuumRepository;

    public VacuumService(VacuumRepository vacuumRepository) {
        this.vacuumRepository = vacuumRepository;
    }

    public List<VacuumProgress> getStatus() {
        return vacuumRepository.findVacuumProgress();
    }

    public List<TableVacuumInfo> getTables(String schema) {
        return vacuumRepository.findTableVacuumStats(schema);
    }

    public Map<String, String> vacuum(String schema, String table) {
        vacuumRepository.vacuum(schema, table, true, false);
        return Map.of("status", "success", "message", "Vacuum started");
    }

    public Map<String, String> analyze(String schema, String table) {
        vacuumRepository.analyze(schema, table);
        return Map.of("status", "success", "message", "Analyze started");
    }
}