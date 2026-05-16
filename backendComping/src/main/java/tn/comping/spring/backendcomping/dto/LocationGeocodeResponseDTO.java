package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO returned by the Google Maps geocoding endpoint.
 */
@Data
@Builder
public class LocationGeocodeResponseDTO {
    private double lat;
    private double lng;
    private String formattedAddress;
}