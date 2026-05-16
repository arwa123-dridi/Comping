package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO pour recevoir la réponse de l'API Flask.
 */
@Data
public class ChecklistResponse {
    private boolean success;
    private String checklistItem;
    private double confidence;
    private String details;
    private String alertLevel;
    private List<String> recommendations;
    private String error;
}