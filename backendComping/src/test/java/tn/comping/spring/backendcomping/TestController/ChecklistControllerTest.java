package tn.comping.spring.backendcomping.TestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.ChecklistController;
import tn.comping.spring.backendcomping.dto.ChecklistRecommandationRequest;
import tn.comping.spring.backendcomping.dto.ChecklistRequest;
import tn.comping.spring.backendcomping.dto.ChecklistResponse;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;
import java.time.LocalDate;

@WebMvcTest(ChecklistController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIChecklistService aiChecklistService;

    @MockBean
    private WeatherService weatherService;

    private ChecklistResponse response;

    @BeforeEach
    void setUp() {
        response = new ChecklistResponse();
        response.setSuccess(true);
        response.setChecklistItem("Chaussures, Eau");
    }

    @Test
    void shouldPredictChecklist() throws Exception {
        ChecklistRequest request = new ChecklistRequest();
        request.setTemperature(20.0);
        request.setPrecipitation(0.0);
        request.setWind_speed(10.0);
        request.setHumidity(40);
        request.setDifficulte(2);

        when(aiChecklistService.predictChecklist(any())).thenReturn(response);

        mockMvc.perform(post("/api/checklist/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklistItem").value("Chaussures, Eau"));

        verify(aiChecklistService).predictChecklist(any());
    }

    @Test
    void shouldReturnChecklistFromWeatherRecommendation() throws Exception {
        ChecklistRecommandationRequest request = new ChecklistRecommandationRequest();
        request.setCity("Tunis");
        request.setDate(LocalDate.of(2026, 6, 1));
        request.setDifficulte(2);

        when(weatherService.getWeather("Tunis", LocalDate.of(2026, 6, 1)))
                .thenReturn(WeatherDTO.builder()
                        .city("Tunis")
                        .date("2026-06-01")
                        .temperature(22)
                        .precipitation(0)
                        .windSpeed(7)
                        .humidity(50)
                        .build());
        when(aiChecklistService.predictChecklist(any())).thenReturn(response);

        mockMvc.perform(post("/api/checklist/recommandation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checklistItem").value("Chaussures, Eau"));

        verify(weatherService).getWeather("Tunis", LocalDate.of(2026, 6, 1));
    }
}
