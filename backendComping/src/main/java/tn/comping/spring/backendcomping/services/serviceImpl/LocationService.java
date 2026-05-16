package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import tn.comping.spring.backendcomping.dto.LocationGeocodeResponseDTO;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Service responsible for resolving a human-readable address into coordinates
 * using the Google Maps Geocoding API.
 */
@Service
public class LocationService {

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    private final RestTemplate restTemplate;
    private final String googleMapsApiKey;

    public LocationService(RestTemplate restTemplate,
                           @Value("${google.maps.api.key:}") String googleMapsApiKey) {
        this.restTemplate = restTemplate;
        this.googleMapsApiKey = googleMapsApiKey;
    }

    /**
     * Resolves an address to its latitude, longitude, and formatted address.
     *
     * @param address free-form address or city name
     * @return geocoding result DTO
     */
    public LocationGeocodeResponseDTO geocodeAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address is required");
        }
        if (googleMapsApiKey == null || googleMapsApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google Maps API key is not configured");
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(GEOCODE_URL)
                .queryParam("address", address)
                .queryParam("key", googleMapsApiKey)
                .build(true)
                .toUri();

        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google Geocoding returned an empty response");
            }

            String status = String.valueOf(response.getOrDefault("status", ""));
            if (!"OK".equals(status)) {
                String message = String.valueOf(response.getOrDefault("error_message", response.getOrDefault("status", "Address not found")));
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found");
            }

            Map<String, Object> first = results.get(0);
            Map<String, Object> geometry = (Map<String, Object>) first.get("geometry");
            Map<String, Object> location = geometry == null ? Map.of() : (Map<String, Object>) geometry.get("location");

            return LocationGeocodeResponseDTO.builder()
                    .lat(extractDouble(location, "lat"))
                    .lng(extractDouble(location, "lng"))
                    .formattedAddress(String.valueOf(first.getOrDefault("formatted_address", address)))
                    .build();
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Google Geocoding request timed out", ex);
        }
    }

    private double extractDouble(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
}