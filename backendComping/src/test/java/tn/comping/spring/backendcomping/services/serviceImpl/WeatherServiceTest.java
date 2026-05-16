package tn.comping.spring.backendcomping.services.serviceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import tn.comping.spring.backendcomping.dto.WeatherForecastResponseDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LocationService locationService;

    @Mock
    private GeocodingService geocodingService;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService(restTemplate, locationService, geocodingService, "test-api-key");
    }

    @Test
    void getForecastByCoordinates_parsesDailyWeatherEntries() {
        Map<String, Object> response = Map.of(
                "cod", "200",
                "city", Map.of("name", "Tunis"),
                "list", List.of(
                        forecastEntry("2026-05-16 09:00:00", 24.0, 23.1, 70, 5.0, 22.0, 26.0, "clear sky", "01d"),
                        forecastEntry("2026-05-17 09:00:00", 25.0, 24.2, 65, 6.5, 23.0, 27.0, "few clouds", "02d"),
                        forecastEntry("2026-05-18 09:00:00", 26.0, 25.0, 60, 7.0, 24.0, 28.0, "scattered clouds", "03d"),
                        forecastEntry("2026-05-19 09:00:00", 27.0, 26.0, 55, 8.0, 25.0, 29.0, "broken clouds", "04d"),
                        forecastEntry("2026-05-20 09:00:00", 28.0, 27.0, 50, 9.0, 26.0, 30.0, "rain", "10d")
                )
        );

        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(response);

        WeatherForecastResponseDTO forecast = weatherService.getForecastByCoordinates(36.8065, 10.1815);

        assertEquals("Tunis", forecast.getCityName());
        assertEquals(5, forecast.getForecast().size());
        assertEquals("2026-05-16", forecast.getForecast().get(0).getDate());
        assertEquals(24.0, forecast.getForecast().get(0).getTemperature(), 0.01);
        assertEquals(23.1, forecast.getForecast().get(0).getFeelsLike(), 0.01);
        assertEquals(70, forecast.getForecast().get(0).getHumidity());
        assertEquals(5.0, forecast.getForecast().get(0).getWindSpeed(), 0.01);
        assertEquals("clear sky", forecast.getForecast().get(0).getDescription());
        assertEquals("01d", forecast.getForecast().get(0).getIconCode());
    }

    private Map<String, Object> forecastEntry(String dateTime,
                                              double temp,
                                              double feelsLike,
                                              int humidity,
                                              double windSpeed,
                                              double minTemp,
                                              double maxTemp,
                                              String description,
                                              String icon) {
        return Map.of(
                "dt_txt", dateTime,
                "main", Map.of(
                        "temp", temp,
                        "feels_like", feelsLike,
                        "humidity", humidity,
                        "temp_min", minTemp,
                        "temp_max", maxTemp
                ),
                "wind", Map.of("speed", windSpeed),
                "weather", List.of(Map.of(
                        "description", description,
                        "icon", icon
                ))
        );
    }
}