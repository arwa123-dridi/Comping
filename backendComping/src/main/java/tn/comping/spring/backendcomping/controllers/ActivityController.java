package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.ActivityService;
import tn.comping.spring.backendcomping.utils.Constants;

import java.util.List;

@RestController
@RequestMapping(Constants.BASE_URL_ACTIVITY)
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;


    @PostMapping(Constants.CREATE_ACTIVITY)
    public ActivityResponse createActivity(@RequestBody ActivityRequest request){
        return activityService.createActivity(request);
    }

    @GetMapping(Constants.GET_ALL_ACTIVITIES)
    public List<ActivityResponse> getAllActivities(){
        return activityService.getAllActivities();
    }


    @GetMapping(Constants.GET_ACTIVITY_BY_ID)
    public ActivityResponse getActivityById(@PathVariable String id){
        return activityService.getActivityById(id);
    }


    @PutMapping(Constants.UPDATE_ACTIVITY)
    public ActivityResponse updateActivity(
            @PathVariable String id,
            @RequestBody ActivityRequest request){

        return activityService.updateActivity(id, request);
    }
    
    @DeleteMapping(Constants.DELETE_ACTIVITY)
    public void deleteActivity(@PathVariable String id){
        activityService.deleteActivity(id);
    }


}
