package tn.comping.spring.backendcomping.services.serviceImpl;

import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.entities.Event;

import java.util.List;

public interface RecomendationActivityService {
    List<Activity> suggestActivities(Event event);
}