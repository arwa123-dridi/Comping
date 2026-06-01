package tn.comping.spring.backendcomping.TestController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.comping.spring.backendcomping.controllers.ActivityController;
import tn.comping.spring.backendcomping.dto.ActivityRequest;
import tn.comping.spring.backendcomping.dto.ActivityResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.ActivityService;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private ActivityController activityController;

    private ActivityRequest request;
    private ActivityResponse response;

    @BeforeEach
    void setUp() {
        request = ActivityRequest.builder()
                .nom("Randonnée")
                .description("Activité en montagne")
                .type("Sport")
                .duree("2h")
                .capacite("10")
                .niveauDifficulte("Moyen")
                .trancheAge("12+")
                .saison("Été")
                .tags(List.of("nature", "sport"))
                .build();

        response = ActivityResponse.builder()
                .idActivity("1")
                .nom("Randonnée")
                .description("Activité en montagne")
                .build();
    }

    @Test
    void createActivity_shouldReturnActivityResponse() {
        when(activityService.createActivity(request)).thenReturn(response);

        ActivityResponse result = activityController.createActivity(request);

        assertNotNull(result);
        assertEquals("1", result.getIdActivity());
        verify(activityService, times(1)).createActivity(request);
    }

    @Test
    void getAllActivities_shouldReturnList() {
        when(activityService.getAllActivities()).thenReturn(List.of(response));

        List<ActivityResponse> result = activityController.getAllActivities();

        assertEquals(1, result.size());
        verify(activityService, times(1)).getAllActivities();
    }

    @Test
    void getActivityById_shouldReturnActivity() {
        when(activityService.getActivityById("1")).thenReturn(response);

        ActivityResponse result = activityController.getActivityById("1");

        assertNotNull(result);
        assertEquals("1", result.getIdActivity());
        verify(activityService).getActivityById("1");
    }

    @Test
    void updateActivity_shouldReturnUpdatedActivity() {
        when(activityService.updateActivity(eq("1"), any(ActivityRequest.class)))
                .thenReturn(response);

        ActivityResponse result = activityController.updateActivity("1", request);

        assertNotNull(result);
        verify(activityService).updateActivity("1", request);
    }

    @Test
    void deleteActivity_shouldCallService() {
        doNothing().when(activityService).deleteActivity("1");

        activityController.deleteActivity("1");

        verify(activityService, times(1)).deleteActivity("1");
    }}