package tn.comping.spring.backendcomping.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO pour recevoir la réponse de l'API Flask.
 */
public class ChecklistResponse {
    private boolean success;
    private String checklistItem;
    private double confidence;
    private String details;
    private String alertLevel;
    private List<String> recommendations;
    private String error;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getChecklistItem() { return checklistItem; }
    public void setChecklistItem(String checklistItem) { this.checklistItem = checklistItem; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}