package tn.comping.spring.backendcomping.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WeatherRequest {
    private String city;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Double latitude;
    private Double longitude;
    private String siteCampingId;
    private String type;
    private String message;
}


