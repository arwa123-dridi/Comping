package tn.comping.spring.backendcomping.services.serviceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import tn.comping.spring.backendcomping.dto.RecommendationRequestDTO;

@Service
@RequiredArgsConstructor
public class RecommendationAIServiceImplProduit implements RecommendationAIServiceProduit {
    private final RestTemplate restTemplate;

    @Value("${ia.api.url}")
    private String FLASK_API_URL;

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRecommendations(List<String> cartProducts) {

        RecommendationRequestDTO request = new RecommendationRequestDTO(cartProducts);

        java.util.Map<String, Object> response = restTemplate.postForObject(
                FLASK_API_URL,
                request,
                java.util.Map.class);

        if (response == null) {
            return java.util.Collections.emptyList();
        }

        Object recommendations = response.get("recommendations");
        if (!(recommendations instanceof List)) {
            return java.util.Collections.emptyList();
        }

        return (List<String>) recommendations;
    }
}