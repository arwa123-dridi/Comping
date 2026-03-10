package tn.comping.spring.backendcomping.utils.mapper;

import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;
import tn.comping.spring.backendcomping.entities.Activity;

public class ActivityMapper {

    public static Activity toEntity(ActivityRequest request){

        return Activity.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .type(request.getType())
                .duree(request.getDuree())
                .capacite(request.getCapacite())
                .build();
    }

    public static ActivityResponse toResponse(Activity activity){

        return ActivityResponse.builder()
                .idActivity(activity.getIdActivity())
                .nom(activity.getNom())
                .description(activity.getDescription())
                .type(activity.getType())
                .duree(activity.getDuree())
                .capacite(activity.getCapacite())
                .build();
    }



}
