package com.ghulam.nova.controller;

import com.ghulam.nova.dtos.TableVacuumInfo;
import com.ghulam.nova.dtos.VacuumProgress;
import com.ghulam.nova.service.VacuumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/backend/api/v1/vacuum")
public class VacuumController {

    private final VacuumService vacuumService;

    public VacuumController(VacuumService vacuumService) {
        this.vacuumService = vacuumService;
    }

    @GetMapping("/status")
    public List<VacuumProgress> getStatus() {
        return vacuumService.getStatus();
    }

    @GetMapping("/tables")
    public List<TableVacuumInfo> getTables(@RequestParam(required = false) String schema) {
        return vacuumService.getTables(schema);
    }

    @PostMapping("/vacuum")
    public Map<String, String> vacuum(@RequestBody Map<String, String> payload) {
        return vacuumService.vacuum(payload.get("schema"), payload.get("table"));
    }

    @PostMapping("/analyze")
    public Map<String, String> analyze(@RequestBody Map<String, String> payload) {
        return vacuumService.analyze(payload.get("schema"), payload.get("table"));
    }
}