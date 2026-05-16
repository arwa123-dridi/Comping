package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherDTO {
    private String city;
    private String date;
    private double temperature;
    private double precipitation;
    private double windSpeed;
    private int humidity;

}
