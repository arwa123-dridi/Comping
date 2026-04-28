package tn.comping.spring.backendcomping.dto;

import lombok.Data;

/**
 * DTO pour envoyer les données à l'API Flask.
 */
@Data
public class ChecklistRequest {
    private double temperature;
    private double precipitation;
    private double windSpeed;
    private double humidity;
    private int difficulte;
}