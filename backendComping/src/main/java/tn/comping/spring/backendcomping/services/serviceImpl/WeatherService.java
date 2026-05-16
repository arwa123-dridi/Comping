package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tn.comping.spring.backendcomping.dto.LocationGeocodeResponseDTO;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.dto.WeatherForecastItemDTO;
import tn.comping.spring.backendcomping.dto.WeatherForecastResponseDTO;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WeatherService {

    private static final String OPEN_WEATHER_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String OPEN_METEO_FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final DateTimeFormatter FORECAST_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    private final WebClient webClient = WebClient.create();
    private final LocationService locationService;
    private final GeocodingService geocodingService;
    private final String openWeatherApiKey;

    public WeatherService(RestTemplate restTemplate,
                          LocationService locationService,
                          GeocodingService geocodingService,
                          @Value("${openweather.api.key:}") String openWeatherApiKey) {
        this.restTemplate = restTemplate;
        this.locationService = locationService;
        this.geocodingService = geocodingService;
        this.openWeatherApiKey = openWeatherApiKey;
    }

    /**
     * Returns the legacy weather payload used by the existing checklist flow.
     *
     * @param city city name
     * @param date requested date
     * @return legacy weather DTO
     */
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

    /**
     * Returns a five-day forecast grouped by day using Google geocoding for the city lookup.
     *
     * @param city city name
     * @return forecast response DTO
     */
    public WeatherForecastResponseDTO getForecastByCity(String city) {
        LocationGeocodeResponseDTO location = locationService.geocodeAddress(city);
        return fetchForecast(location.getLat(), location.getLng(), location.getFormattedAddress());
    }

    /**
     * Returns a five-day forecast for a specific coordinate pair.
     *
     * @param lat latitude
     * @param lon longitude
     * @return forecast response DTO
     */
    public WeatherForecastResponseDTO getForecastByCoordinates(Double lat, Double lon) {
        return fetchForecast(lat, lon, null);
    }

    private WeatherForecastResponseDTO fetchForecast(double lat, double lon, String requestedLabel) {
        Map<String, Object> response;
        try {
            response = executeForecastCall(lat, lon);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED || ex.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return fetchForecastFromOpenMeteo(lat, lon, requestedLabel);
            }
            throw ex;
        }

        Map<String, Object> cityInfo = asMap(response.get("city"));
        String cityName = requestedLabel != null && !requestedLabel.isBlank()
                ? requestedLabel
                : Optional.ofNullable(cityInfo)
                .map(city -> String.valueOf(city.getOrDefault("name", "Unknown")))
                .orElse("Unknown");

        List<Map<String, Object>> list = asListOfMaps(response.get("list"));
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No weather forecast available for the selected location");
        }

        List<WeatherForecastItemDTO> forecast = list.stream()
                .collect(Collectors.groupingBy(
                        this::extractForecastDay,
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet()
                .stream()
                .limit(5)
                .map(entry -> toDailyForecast(cityName, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return WeatherForecastResponseDTO.builder()
                .cityName(cityName)
                .latitude(lat)
                .longitude(lon)
                .formattedAddress(requestedLabel)
                .forecast(forecast)
                .build();
    }

    private Map<String, Object> executeForecastCall(double lat, double lon) {
        if (openWeatherApiKey == null || openWeatherApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OpenWeatherMap API key is not configured");
        }

        URI uri = UriComponentsBuilder
                .fromHttpUrl(OPEN_WEATHER_FORECAST_URL)
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", openWeatherApiKey)
                .queryParam("units", "metric")
                .queryParam("cnt", 40)
                .build(true)
                .toUri();

        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenWeatherMap returned an empty response");
            }

            Object cod = response.get("cod");
            if (cod != null && !"200".equals(String.valueOf(cod))) {
                if ("401".equals(String.valueOf(cod))) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                            "OpenWeatherMap error: " + response.getOrDefault("message", "invalid API key"));
                }
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "OpenWeatherMap error: " + response.getOrDefault("message", "unknown error"));
            }

            return response;
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "OpenWeatherMap error: invalid API key", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "OpenWeatherMap request failed: " + ex.getStatusText(), ex);
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "OpenWeatherMap request timed out", ex);
        }
    }

    private WeatherForecastResponseDTO fetchForecastFromOpenMeteo(double lat, double lon, String requestedLabel) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(OPEN_METEO_FORECAST_URL)
                .queryParam("latitude", lat)
                .queryParam("longitude", lon)
                .queryParam("daily", "temperature_2m_max,temperature_2m_min,wind_speed_10m_max,weather_code")
                .queryParam("current", "temperature_2m,wind_speed_10m,weather_code")
                .queryParam("timezone", "auto")
                .queryParam("forecast_days", 5)
                .build(true)
                .toUri();

        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Open-Meteo returned an empty response");
            }

            Map<String, Object> daily = asMap(response.get("daily"));
            List<String> times = asStringList(daily.get("time"));
            List<Number> maxTemps = asNumberList(daily.get("temperature_2m_max"));
            List<Number> minTemps = asNumberList(daily.get("temperature_2m_min"));
            List<Number> winds = asNumberList(daily.get("wind_speed_10m_max"));
            List<Number> weatherCodes = asNumberList(daily.get("weather_code"));

            if (times.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No weather forecast available for the selected location");
            }

            List<WeatherForecastItemDTO> forecast = new ArrayList<>();
            for (int index = 0; index < Math.min(5, times.size()); index++) {
                double min = numberAt(minTemps, index, 0.0);
                double max = numberAt(maxTemps, index, min);
                double avg = (min + max) / 2.0;
                int weatherCode = (int) Math.round(numberAt(weatherCodes, index, 0.0));

                forecast.add(WeatherForecastItemDTO.builder()
                        .cityName(requestedLabel != null && !requestedLabel.isBlank() ? requestedLabel : "Selected location")
                        .date(times.get(index))
                        .temperature(avg)
                        .feelsLike(avg)
                        .humidity(0)
                        .windSpeed(numberAt(winds, index, 0.0))
                        .description(openMeteoDescription(weatherCode))
                        .iconCode(openMeteoIconCode(weatherCode))
                        .minTemperature(min)
                        .maxTemperature(max)
                        .build());
            }

            return WeatherForecastResponseDTO.builder()
                    .cityName(requestedLabel != null && !requestedLabel.isBlank() ? requestedLabel : "Selected location")
                    .latitude(lat)
                    .longitude(lon)
                    .formattedAddress(requestedLabel)
                    .forecast(forecast)
                    .build();
        } catch (ResourceAccessException ex) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "Open-Meteo request timed out", ex);
        }
    }

    private WeatherForecastItemDTO toDailyForecast(String cityName, String day, List<Map<String, Object>> entries) {
        Map<String, Object> first = entries.get(0);
        Map<String, Object> firstMain = asMap(first.get("main"));
        Map<String, Object> firstWind = asMap(first.get("wind"));
        Map<String, Object> firstWeather = firstWeather(first);

        double averageTemperature = entries.stream()
                .mapToDouble(entry -> extractDouble(asMap(entry.get("main")), "temp", 0.0))
                .average()
                .orElse(0.0);
        double averageFeelsLike = entries.stream()
                .mapToDouble(entry -> extractDouble(asMap(entry.get("main")), "feels_like", 0.0))
                .average()
                .orElse(0.0);
        double minTemperature = entries.stream()
                .mapToDouble(entry -> extractDouble(asMap(entry.get("main")), "temp_min",
                        extractDouble(asMap(entry.get("main")), "temp", 0.0)))
                .min()
                .orElse(0.0);
        double maxTemperature = entries.stream()
                .mapToDouble(entry -> extractDouble(asMap(entry.get("main")), "temp_max",
                        extractDouble(asMap(entry.get("main")), "temp", 0.0)))
                .max()
                .orElse(0.0);
        double windSpeed = entries.stream()
                .mapToDouble(entry -> extractDouble(asMap(entry.get("wind")), "speed", 0.0))
                .max()
                .orElse(extractDouble(firstWind, "speed", 0.0));

        return WeatherForecastItemDTO.builder()
                .cityName(cityName)
                .date(day)
                .temperature(averageTemperature)
                .feelsLike(averageFeelsLike)
                .humidity(extractInt(firstMain, "humidity", 0))
                .windSpeed(windSpeed)
                .description(String.valueOf(firstWeather.getOrDefault("description", "Unknown")))
                .iconCode(String.valueOf(firstWeather.getOrDefault("icon", "")))
                .minTemperature(minTemperature)
                .maxTemperature(maxTemperature)
                .build();
    }

    private String extractForecastDay(Map<String, Object> forecastEntry) {
        Object dateTime = forecastEntry.get("dt_txt");
        if (dateTime != null) {
            return LocalDateTime.parse(String.valueOf(dateTime), FORECAST_DATE_TIME).toLocalDate().toString();
        }
        Object dt = forecastEntry.get("dt");
        if (dt instanceof Number number) {
            return java.time.Instant.ofEpochSecond(number.longValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString();
        }
        return LocalDate.now().toString();
    }

    private Map<String, Object> firstWeather(Map<String, Object> forecastEntry) {
        List<Map<String, Object>> weather = asListOfMaps(forecastEntry.get("weather"));
        if (weather.isEmpty()) {
            return Map.of();
        }
        return weather.get(0);
    }

    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private List<Map<String, Object>> asListOfMaps(Object value) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (Object element : list) {
                if (element instanceof Map<?, ?> map) {
                    result.add((Map<String, Object>) map);
                }
            }
            return result;
        }
        return List.of();
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object element : list) {
                if (element != null) {
                    result.add(String.valueOf(element));
                }
            }
            return result;
        }
        return List.of();
    }

    private List<Number> asNumberList(Object value) {
        if (value instanceof List<?> list) {
            List<Number> result = new ArrayList<>();
            for (Object element : list) {
                if (element instanceof Number number) {
                    result.add(number);
                }
            }
            return result;
        }
        return List.of();
    }

    private double numberAt(List<Number> values, int index, double defaultValue) {
        if (index < 0 || index >= values.size()) {
            return defaultValue;
        }
        Number number = values.get(index);
        return number == null ? defaultValue : number.doubleValue();
    }

    private String openMeteoDescription(int code) {
        return switch (code) {
            case 0 -> "Ciel dégagé";
            case 1, 2 -> "Partiellement nuageux";
            case 3 -> "Couvert";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55, 56, 57 -> "Bruine";
            case 61, 63, 65, 66, 67, 80, 81, 82 -> "Pluie";
            case 71, 73, 75, 77, 85, 86 -> "Neige";
            case 95, 96, 99 -> "Orage";
            default -> "Conditions variables";
        };
    }

    private String openMeteoIconCode(int code) {
        return switch (code) {
            case 0 -> "01d";
            case 1, 2 -> "02d";
            case 3 -> "04d";
            case 45, 48 -> "50d";
            case 51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "10d";
            case 71, 73, 75, 77, 85, 86 -> "13d";
            case 95, 96, 99 -> "11d";
            default -> "03d";
        };
    }

    private double extractDouble(Map<String, Object> source, String key, double defaultValue) {
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    private int extractInt(Map<String, Object> source, String key, int defaultValue) {
        Object value = source.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
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