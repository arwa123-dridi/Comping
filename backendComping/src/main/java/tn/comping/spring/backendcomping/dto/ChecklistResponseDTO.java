package tn.comping.spring.backendcomping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class ChecklistResponseDTO {
    private String destination;
    private int durationDays;
    private String difficulty;
    private String season;
    private Map<String, List<String>> categories; // Category -> List of items

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public Map<String, List<String>> getCategories() { return categories; }
    public void setCategories(Map<String, List<String>> categories) { this.categories = categories; }

    public static ChecklistResponseDTOBuilder builder() {
        return new ChecklistResponseDTOBuilder();
    }

    public static class ChecklistResponseDTOBuilder {
        private ChecklistResponseDTO dto = new ChecklistResponseDTO();
        public ChecklistResponseDTOBuilder destination(String destination) { dto.setDestination(destination); return this; }
        public ChecklistResponseDTOBuilder durationDays(int durationDays) { dto.setDurationDays(durationDays); return this; }
        public ChecklistResponseDTOBuilder difficulty(String difficulty) { dto.setDifficulty(difficulty); return this; }
        public ChecklistResponseDTOBuilder season(String season) { dto.setSeason(season); return this; }
        public ChecklistResponseDTOBuilder categories(Map<String, List<String>> categories) { dto.setCategories(categories); return this; }
        public ChecklistResponseDTO build() { return dto; }
    }
}
