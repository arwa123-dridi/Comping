package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.comping.spring.backendcomping.dto.RecommendationRequestDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationAIServiceImplProduit implements RecommendationAIServiceProduit {
    private final RestTemplate restTemplate;

    private final String FLASK_API_URL = "http://127.0.0.1:5000/recommend";

    @Override
    public List<String> getRecommendations(List<String> cartProducts) {

        RecommendationRequestDTO request = new RecommendationRequestDTO(cartProducts);

        @SuppressWarnings("unchecked")
        var response = restTemplate.postForObject(
                FLASK_API_URL,
                request,
                java.util.Map.class);

        return (List<String>) response.get("recommendations");
    }
}
