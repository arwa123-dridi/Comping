package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO returned by the weather forecast endpoint.
 */
@Data
@Builder
public class WeatherForecastResponseDTO {
    private String cityName;
    private double latitude;
    private double longitude;
    private String formattedAddress;
    private List<WeatherForecastItemDTO> forecast;
}