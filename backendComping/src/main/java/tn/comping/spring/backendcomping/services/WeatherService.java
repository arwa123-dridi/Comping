package tn.comping.spring.backendcomping.services;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// @Service
public class WeatherService {

    private final WebClient webClient = WebClient.create();

    @Autowired
    private GeocodingService geocodingService;

    public WeatherDTO getWeather(String city, LocalDate date) {
        double[] coords = geocodingService.getCoordinates(city);
        double lat = coords[0];
        double lon = coords[1];

        if (!date.isAfter(LocalDate.now())) {
            return getFromArchive(lat, lon, date, city);
        } else {
            return getFromForecast(lat, lon, date, city);
        }
    }

    // Dates passées → Open-Meteo Archive (sans humidité)
    private WeatherDTO getFromArchive(double lat, double lon, LocalDate date, String city) {
        String dateStr = date.toString();

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://archive-api.open-meteo.com/v1/archive")
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("start_date", dateStr)
                .queryParam("end_date", dateStr)
                .queryParam("daily", "temperature_2m_mean,precipitation_sum,wind_speed_10m_max") //  plus d'humidité
                .queryParam("timezone", "Africa/Tunis")
                .build()
                .toUri();

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        Map<String, Object> daily = (Map<String, Object>) response.get("daily");

        return WeatherDTO.builder()
                .city(city)
                .date(dateStr)
                .temperature(extractValue(daily, "temperature_2m_mean", 0))
                .precipitation(extractValue(daily, "precipitation_sum", 0))
                .windSpeed(extractValue(daily, "wind_speed_10m_max", 0))
                .humidity(50) // valeur par défaut
                .build();
    }

    // Dates futures → Open-Meteo Forecast (sans humidité)
    private WeatherDTO getFromForecast(double lat, double lon, LocalDate date, String city) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("daily", "temperature_2m_max,precipitation_sum,wind_speed_10m_max") // plus d'humidité
                .queryParam("timezone", "Africa/Tunis")
                .queryParam("forecast_days", 16)
                // .queryParam("past_days", 5) // supprimé car inutile pour une date future
                .build()
                .toUri();

        Map<String, Object> response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        Map<String, Object> daily = (Map<String, Object>) response.get("daily");
        List<String> times = (List<String>) daily.get("time");
        int index = times.indexOf(date.toString());

        if (index == -1) {
            throw new RuntimeException("Date non disponible : " + date
                    + " | Plage dispo : " + times.get(0) + " → " + times.get(times.size() - 1));
        }

        return WeatherDTO.builder()
                .city(city)
                .date(date.toString())
                .temperature(extractValue(daily, "temperature_2m_max", index))
                .precipitation(extractValue(daily, "precipitation_sum", index))
                .windSpeed(extractValue(daily, "wind_speed_10m_max", index))
                .humidity(50)
                .build();
    }

    private double extractValue(Map<String, Object> daily, String key, int index) {
        List<Number> values = (List<Number>) daily.get(key);
        if (values == null || values.size() <= index || values.get(index) == null) return 0.0;
        return values.get(index).doubleValue();
    }
}