package com.ghulam.bubble.controller;

import com.ghulam.bubble.dtos.ActivitySession;
import com.ghulam.bubble.service.ActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/backend/api/v1/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivitySession> getActivity(
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "100") int limit) {
        return activityService.getAllActivities(state, limit);
    }

    @GetMapping("/active")
    public List<ActivitySession> getActive() {
        return activityService.getActiveActivities();
    }

    @GetMapping("/idle")
    public List<ActivitySession> getIdle() {
        return activityService.getIdleActivities();
    }

    @GetMapping("/waiting")
    public List<ActivitySession> getWaiting() {
        return activityService.getWaitingActivities();
    }

    @GetMapping("/long-running")
    public List<ActivitySession> getLongRunning(
            @RequestParam(defaultValue = "30.0") double threshold) {
        return activityService.getLongRunningActivities(threshold);
    }

    @GetMapping("/{pid}")
    public ResponseEntity<ActivitySession> getByPid(@PathVariable int pid) {
        return activityService.getActivityByPid(pid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}