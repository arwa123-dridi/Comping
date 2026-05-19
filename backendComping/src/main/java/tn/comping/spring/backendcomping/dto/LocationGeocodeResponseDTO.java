package tn.comping.spring.backendcomping.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO returned by the Google Maps geocoding endpoint.
 */
@NoArgsConstructor
@AllArgsConstructor
public class LocationGeocodeResponseDTO {
    private double lat;
    private double lng;
    private String formattedAddress;

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public String getFormattedAddress() { return formattedAddress; }
    public void setFormattedAddress(String formattedAddress) { this.formattedAddress = formattedAddress; }

    public static LocationGeocodeResponseDTOBuilder builder() {
        return new LocationGeocodeResponseDTOBuilder();
    }

    public static class LocationGeocodeResponseDTOBuilder {
        private LocationGeocodeResponseDTO dto = new LocationGeocodeResponseDTO();
        public LocationGeocodeResponseDTOBuilder lat(double lat) { dto.setLat(lat); return this; }
        public LocationGeocodeResponseDTOBuilder lng(double lng) { dto.setLng(lng); return this; }
        public LocationGeocodeResponseDTOBuilder formattedAddress(String formattedAddress) { dto.setFormattedAddress(formattedAddress); return this; }
        public LocationGeocodeResponseDTO build() { return dto; }
    }
}