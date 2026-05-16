package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Represents a daily weather snapshot extracted from the OpenWeatherMap forecast response.
 */
@Data
@Builder
public class WeatherForecastItemDTO {
    private String cityName;
    private String date;
    private double temperature;
    private double feelsLike;
    private int humidity;
    private double windSpeed;
    private String description;
    private String iconCode;
    private double minTemperature;
    private double maxTemperature;
}