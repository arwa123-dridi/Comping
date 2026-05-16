package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class WeatherAlert {
    private String id;
    private String type; // STORM, FLOOD, HEAT, COLD, WIND, GENERAL
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private String message;
    private Date startTime;
    private Date endTime;
    private List<String> affectedZones;
    private String siteCampingId;
}