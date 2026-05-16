package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.comping.spring.backendcomping.dto.LocationGeocodeResponseDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.LocationService;

/**
 * REST controller exposing Google Maps geocoding results to the frontend.
 */
@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * Geocodes a city or address into latitude and longitude.
     *
     * @param address free-form address or city name
     * @return geocoding DTO
     */
    @GetMapping("/geocode")
    public ResponseEntity<LocationGeocodeResponseDTO> geocode(@RequestParam String address) {
        return ResponseEntity.ok(locationService.geocodeAddress(address));
    }
}