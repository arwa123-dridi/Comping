package tn.comping.spring.backendcomping.Testunitaire.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;
import tn.comping.spring.backendcomping.entities.Activity;
import tn.comping.spring.backendcomping.repositories.ActivityRepository;
import tn.comping.spring.backendcomping.services.serviceImpl.ActivityServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivityServiceImplTest {

    @Mock
    private ActivityRepository activityRepository;

    @InjectMocks
    private ActivityServiceImpl activityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateActivity() {

        ActivityRequest request = new ActivityRequest();
        request.setNom("Football");

        Activity activity = new Activity();
        activity.setIdActivity("1");
        activity.setNom("Football");

        when(activityRepository.save(any(Activity.class)))
                .thenReturn(activity);

        ActivityResponse response = activityService.createActivity(request);

        assertNotNull(response);
        assertEquals("Football", response.getNom());

        verify(activityRepository, times(1))
                .save(any(Activity.class));
    }

    @Test
    void testGetAllActivities() {

        Activity activity1 = new Activity();
        activity1.setNom("Football");

        Activity activity2 = new Activity();
        activity2.setNom("Tennis");

        when(activityRepository.findAll())
                .thenReturn(List.of(activity1, activity2));

        List<ActivityResponse> responses =
                activityService.getAllActivities();

        assertEquals(2, responses.size());

        verify(activityRepository, times(1))
                .findAll();
    }

    @Test
    void testGetActivityById() {

        Activity activity = new Activity();
        activity.setIdActivity("1");
        activity.setNom("Basket");

        when(activityRepository.findById("1"))
                .thenReturn(Optional.of(activity));

        ActivityResponse response =
                activityService.getActivityById("1");

        assertNotNull(response);
        assertEquals("Basket", response.getNom());
    }

    @Test
    void testDeleteActivity() {

        doNothing().when(activityRepository)
                .deleteById("1");

        activityService.deleteActivity("1");

        verify(activityRepository, times(1))
                .deleteById("1");
    }

    @Test
    void testUpdateActivity() {

        ActivityRequest request = new ActivityRequest();
        request.setNom("Updated Activity");

        Activity activity = new Activity();
        activity.setIdActivity("1");
        activity.setNom("Old Activity");

        when(activityRepository.findById("1"))
                .thenReturn(Optional.of(activity));

        when(activityRepository.save(any(Activity.class)))
                .thenReturn(activity);

        ActivityResponse response =
                activityService.updateActivity("1", request);

        assertEquals("Updated Activity", response.getNom());

        verify(activityRepository).save(activity);
    }
}