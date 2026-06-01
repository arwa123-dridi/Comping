package tn.comping.spring.backendcomping.dto;

import java.util.List;

public class RecommendationResponseDTO {

    private List<String> recommendations;

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}