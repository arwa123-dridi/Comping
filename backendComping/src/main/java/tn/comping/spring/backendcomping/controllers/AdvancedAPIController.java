package tn.comping.spring.backendcomping.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.services.serviceImpl.AdvancedAPIService;
import java.util.List;

@RestController
@RequestMapping("/api/advanced")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdvancedAPIController {

    private final AdvancedAPIService advancedAPIService;

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            return ResponseEntity.ok(advancedAPIService.getWeatherByCoordinates(latitude, longitude));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching weather: " + e.getMessage());
        }
    }

    @GetMapping("/weather-alerts")
    public ResponseEntity<?> getWeatherAlerts(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            return ResponseEntity.ok(advancedAPIService.getWeatherAlerts(latitude, longitude));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching weather alerts: " + e.getMessage());
        }
    }

    @GetMapping("/location")
    public ResponseEntity<?> getLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            return ResponseEntity.ok(advancedAPIService.getLocationBCoordinates(latitude, longitude));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching location: " + e.getMessage());
        }
    }

    @GetMapping("/emergency-services")
    public ResponseEntity<?> getNearbyEmergencyServices(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String type) {
        try {
            return ResponseEntity.ok(advancedAPIService.getNearbyEmergencyServices(latitude, longitude, type));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching emergency services: " + e.getMessage());
        }
    }

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(
            @RequestParam Double startLat,
            @RequestParam Double startLng,
            @RequestParam Double endLat,
            @RequestParam Double endLng) {
        try {
            return ResponseEntity.ok(advancedAPIService.getRoute(startLat, startLng, endLat, endLng));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating route: " + e.getMessage());
        }
    }

    @PostMapping("/notification")
    public ResponseEntity<?> sendNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam List<String> recipients,
            @RequestParam(defaultValue = "HIGH") String priority) {
        try {
            return ResponseEntity.ok(advancedAPIService.sendEmergencyNotification(title, message, recipients, priority));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending notification: " + e.getMessage());
        }
    }

    @GetMapping("/traffic")
    public ResponseEntity<?> getTraffic(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            return ResponseEntity.ok(advancedAPIService.getTrafficData(latitude, longitude));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching traffic data: " + e.getMessage());
        }
    }
}
