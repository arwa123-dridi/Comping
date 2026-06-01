package tn.comping.spring.backendcomping.Testunitaire.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import tn.comping.spring.backendcomping.dto.ChecklistRequest;
import tn.comping.spring.backendcomping.dto.ChecklistResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;

@ExtendWith(MockitoExtension.class)
class ChecklistServiceTest {

    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private AIChecklistService checklistService;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(checklistService, "flastApiUrl", "http://fake-url");
    }

    @Test
    void shouldReturnFailureWhenApiUnavailable() {
        ChecklistRequest request = new ChecklistRequest();
        request.setTemperature(10.0);
        request.setPrecipitation(1.0);
        request.setWind_speed(5.0);
        request.setHumidity(80);
        request.setDifficulte(3);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ChecklistResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ChecklistResponse response = checklistService.predictChecklist(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("Service IA indisponible"));
    }

    @Test
    void shouldReturnChecklistResponseWhenApiReturnsSuccess() {
        ChecklistRequest request = new ChecklistRequest();
        request.setTemperature(20.0);
        request.setPrecipitation(0.0);
        request.setWind_speed(10.0);
        request.setHumidity(50);
        request.setDifficulte(2);

        ChecklistResponse apiResponse = new ChecklistResponse();
        apiResponse.setSuccess(true);
        apiResponse.setChecklistItem("Chaussures, Eau");
        apiResponse.setConfidence(0.92);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ChecklistResponse.class)))
                .thenReturn(ResponseEntity.ok(apiResponse));

        ChecklistResponse response = checklistService.predictChecklist(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Chaussures, Eau", response.getChecklistItem());
    }
}