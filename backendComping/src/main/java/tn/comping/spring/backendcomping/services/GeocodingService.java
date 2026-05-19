package tn.comping.spring.backendcomping.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

// @Service
public class GeocodingService {

    private final WebClient webClient = WebClient.create();

    @Cacheable("coordinates")
    public double[] getCoordinates(String city) {
        String url = "https://geocoding-api.open-meteo.com/v1/search"
                + "?name=" + city
                + "&count=1"
                + "&language=fr";

        Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("Ville introuvable : " + city);
        }

        Map<String, Object> first = results.get(0);
        double lat = (double) first.get("latitude");
        double lon = (double) first.get("longitude");
        return new double[]{lat, lon};
    }
}