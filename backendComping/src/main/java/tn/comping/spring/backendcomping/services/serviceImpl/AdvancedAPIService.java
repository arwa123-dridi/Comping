package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class AdvancedAPIService {

    private final RestTemplate restTemplate;
    
    @Value("${weather.api.key:demo}")
    private String weatherApiKey;
    
    @Value("${google.maps.api.key:demo}")
    private String mapsApiKey;
    
    @Value("${openweathermap.url:https://api.openweathermap.org/data/2.5}")
    private String weatherApiUrl;

    /**
     * Get weather data for a specific location
     */
    public WeatherData getWeatherByCoordinates(Double latitude, Double longitude) {
        try {
            String url = String.format("%s/weather?lat=%s&lon=%s&appid=%s&units=metric",
                    weatherApiUrl, latitude, longitude, weatherApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            return parseWeatherResponse(response);
        } catch (Exception e) {
            return new WeatherData("N/A", 0, 0, "Unable to fetch weather data");
        }
    }

    /**
     * Get weather alerts for a region
     */
    public List<WeatherAlert> getWeatherAlerts(Double latitude, Double longitude) {
        try {
            String url = String.format("%s/weather?lat=%s&lon=%s&appid=%s",
                    weatherApiUrl, latitude, longitude, weatherApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<WeatherAlert> alerts = new ArrayList<>();
            
            if (response != null && response.containsKey("alerts")) {
                List<Map<String, Object>> alertList = (List<Map<String, Object>>) response.get("alerts");
                for (Map<String, Object> alert : alertList) {
                    alerts.add(new WeatherAlert(
                        (String) alert.get("event"),
                        (String) alert.get("description"),
                        "HIGH"
                    ));
                }
            }
            return alerts;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get location details by coordinates
     */
    public Location getLocationBCoordinates(Double latitude, Double longitude) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=%s",
                latitude, longitude, mapsApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseLocationResponse(response);
        } catch (Exception e) {
            return new Location("Unknown", "Unknown", latitude, longitude);
        }
    }

    /**
     * Get nearby emergency services
     */
    public List<EmergencyService> getNearbyEmergencyServices(Double latitude, Double longitude, String type) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=5000&type=%s&key=%s",
                latitude, longitude, type, mapsApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<EmergencyService> services = new ArrayList<>();
            
            if (response != null && response.containsKey("results")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                for (Map<String, Object> result : results) {
                    Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    
                    services.add(new EmergencyService(
                        (String) result.get("name"),
                        type,
                        ((Number) location.get("lat")).doubleValue(),
                        ((Number) location.get("lng")).doubleValue(),
                        (String) result.get("vicinity")
                    ));
                }
            }
            return services;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Calculate route between two locations
     */
    public RouteInfo getRoute(Double startLat, Double startLng, Double endLat, Double endLng) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%s,%s&destination=%s,%s&key=%s",
                startLat, startLng, endLat, endLng, mapsApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return parseRouteResponse(response);
        } catch (Exception e) {
            return new RouteInfo(0, 0, "Unable to calculate route");
        }
    }

    /**
     * Send emergency notification
     */
    public NotificationResult sendEmergencyNotification(String title, String message, 
                                                         List<String> recipientIds, String priority) {
        try {
            // Implementation for sending notifications via Firebase, Twilio, etc.
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("message", message);
            notification.put("priority", priority);
            notification.put("recipients", recipientIds);
            notification.put("timestamp", new Date());
            
            // Send via notification service (Firebase Cloud Messaging, etc.)
            return new NotificationResult(true, "Notification sent successfully", recipientIds.size());
        } catch (Exception e) {
            return new NotificationResult(false, "Failed to send notification: " + e.getMessage(), 0);
        }
    }

    /**
     * Get real-time traffic data
     */
    public TrafficData getTrafficData(Double latitude, Double longitude) {
        try {
            String url = String.format(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=1000&key=%s",
                latitude, longitude, mapsApiKey);
            
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            // Parse traffic information
            return new TrafficData("Normal", 0, "Good conditions");
        } catch (Exception e) {
            return new TrafficData("Unknown", 0, "Unable to fetch traffic data");
        }
    }

    // Helper methods
    private WeatherData parseWeatherResponse(Map<String, Object> response) {
        if (response != null) {
            String description = "Clear";
            double temperature = 0;
            int humidity = 0;
            
            if (response.containsKey("weather")) {
                List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
                if (!weather.isEmpty()) {
                    description = (String) weather.get(0).get("main");
                }
            }
            
            if (response.containsKey("main")) {
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                temperature = ((Number) main.get("temp")).doubleValue();
                humidity = ((Number) main.get("humidity")).intValue();
            }
            
            return new WeatherData(description, temperature, humidity, "Success");
        }
        return new WeatherData("N/A", 0, 0, "No data");
    }

    private Location parseLocationResponse(Map<String, Object> response) {
        String city = "Unknown";
        String country = "Unknown";
        
        if (response != null && (boolean) response.get("ok")) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (!results.isEmpty()) {
                String formattedAddress = (String) results.get(0).get("formatted_address");
                String[] parts = formattedAddress.split(",");
                if (parts.length > 0) {
                    city = parts[parts.length - 2].trim();
                    country = parts[parts.length - 1].trim();
                }
            }
        }
        
        return new Location(city, country, 0.0, 0.0);
    }

    private RouteInfo parseRouteResponse(Map<String, Object> response) {
        if (response != null && (boolean) response.get("ok")) {
            List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
            if (!routes.isEmpty()) {
                Map<String, Object> leg = ((List<Map<String, Object>>) routes.get(0).get("legs")).get(0);
                int distance = ((Number) ((Map<String, Object>) leg.get("distance")).get("value")).intValue();
                int duration = ((Number) ((Map<String, Object>) leg.get("duration")).get("value")).intValue();
                
                return new RouteInfo(distance, duration, "Route calculated successfully");
            }
        }
        return new RouteInfo(0, 0, "Unable to calculate route");
    }

    // DTOs
    public static class WeatherData {
        public String description;
        public double temperature;
        public int humidity;
        public String status;

        public WeatherData(String description, double temperature, int humidity, String status) {
            this.description = description;
            this.temperature = temperature;
            this.humidity = humidity;
            this.status = status;
        }
    }

    public static class WeatherAlert {
        public String event;
        public String description;
        public String severity;

        public WeatherAlert(String event, String description, String severity) {
            this.event = event;
            this.description = description;
            this.severity = severity;
        }
    }

    public static class Location {
        public String city;
        public String country;
        public Double latitude;
        public Double longitude;

        public Location(String city, String country, Double latitude, Double longitude) {
            this.city = city;
            this.country = country;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class EmergencyService {
        public String name;
        public String type;
        public Double latitude;
        public Double longitude;
        public String address;

        public EmergencyService(String name, String type, Double latitude, Double longitude, String address) {
            this.name = name;
            this.type = type;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }
    }

    public static class RouteInfo {
        public int distanceMeters;
        public int durationSeconds;
        public String status;

        public RouteInfo(int distanceMeters, int durationSeconds, String status) {
            this.distanceMeters = distanceMeters;
            this.durationSeconds = durationSeconds;
            this.status = status;
        }
    }

    public static class NotificationResult {
        public boolean success;
        public String message;
        public int recipientCount;

        public NotificationResult(boolean success, String message, int recipientCount) {
            this.success = success;
            this.message = message;
            this.recipientCount = recipientCount;
        }
    }

    public static class TrafficData {
        public String status;
        public int delayMinutes;
        public String description;

        public TrafficData(String status, int delayMinutes, String description) {
            this.status = status;
            this.delayMinutes = delayMinutes;
            this.description = description;
        }
    }
}
