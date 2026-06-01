package tn.comping.spring.backendcomping.TestController;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tn.comping.spring.backendcomping.controllers.WeatherController;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WeatherService weatherService;

    @Test
    void shouldReturnWeatherForCityAndDate() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 1);
        when(weatherService.getWeather("Tunis", date)).thenReturn(
                WeatherDTO.builder()
                        .city("Tunis")
                        .date("2026-06-01")
                        .temperature(24)
                        .precipitation(0)
                        .windSpeed(12)
                        .humidity(65)
                        .build());

        mockMvc.perform(get("/api/weather")
                        .param("city", "Tunis")
                        .param("date", "2026-06-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Tunis"))
                .andExpect(jsonPath("$.temperature").value(24.0));
    }
}
