package tn.comping.spring.backendcomping.services;

import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;

import java.util.List;

public interface ActivityService {
    ActivityResponse createActivity(ActivityRequest request);

    List<ActivityResponse> getAllActivities();

    ActivityResponse getActivityById(String id);

    ActivityResponse updateActivity(String id, ActivityRequest request);

    void deleteActivity(String id);
}
