package tn.comping.spring.backendcomping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO pour envoyer les données à l'API Flask.
 */
@Data
public class ChecklistRequest {
    private double temperature;
    private double precipitation;
    @JsonProperty("wind_speed")
    private double wind_speed;
    private double humidity;
    private int difficulte;


}