package com.ghulam.nova.service;

import com.ghulam.nova.dtos.ActivitySession;
import com.ghulam.nova.repo.ActivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<ActivitySession> getAllActivities(String state, int limit) {
        return activityRepository.findAll(state, limit);
    }

    public Optional<ActivitySession> getActivityByPid(int pid) {
        return activityRepository.findByPid(pid);
    }

    public List<ActivitySession> getActiveActivities() {
        return activityRepository.findActive();
    }

    public List<ActivitySession> getIdleActivities() {
        return activityRepository.findIdle();
    }

    public List<ActivitySession> getWaitingActivities() {
        return activityRepository.findWaiting();
    }

    public List<ActivitySession> getLongRunningActivities(double thresholdSeconds) {
        return activityRepository.findLongRunning(thresholdSeconds);
    }
}