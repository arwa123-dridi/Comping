package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.utils.mapper.ActivityMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements  ActivityService{
    private final ActivityRepository activityRepository;

    @Override
    public ActivityResponse createActivity(ActivityRequest request) {
        Activity activity = ActivityMapper.toEntity(request);

        activity = activityRepository.save(activity);

        return ActivityMapper.toResponse(activity);
    }

    @Override
    public List<ActivityResponse> getAllActivities() {
        return activityRepository.findAll()
                .stream()
                .map(ActivityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ActivityResponse getActivityById(String id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        return ActivityMapper.toResponse(activity);
    }

    @Override
    public ActivityResponse updateActivity(String id, ActivityRequest request) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found"));

        activity.setNom(request.getNom());
        activity.setDescription(request.getDescription());
        activity.setType(request.getType());
        activity.setDuree(request.getDuree());
        activity.setCapacite(request.getCapacite());

        activity.setNiveauDifficulte(request.getNiveauDifficulte());
        activity.setTrancheAge(request.getTrancheAge());
        activity.setSaison(request.getSaison());
        activity.setTags(request.getTags());
        activity = activityRepository.save(activity);

        return ActivityMapper.toResponse(activity);
    }

    @Override
    public void deleteActivity(String id) {
        activityRepository.deleteById(id);
    }
}