package tn.comping.spring.backendcomping.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.dto.WeatherForecastResponseDTO;
import tn.comping.spring.backendcomping.dto.WeatherRequest;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;

/**
 * REST controller exposing both the legacy weather endpoint and the new forecast endpoint.
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "http://localhost:4200")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    /**
     * Legacy endpoint used by the existing checklist flow.
     *
     * @param request city/date request payload
     * @return legacy weather DTO
     */
    @PostMapping
    public ResponseEntity<WeatherDTO> getWeather(@RequestBody WeatherRequest request) {
        WeatherDTO result = weatherService.getWeather(request.getCity(), request.getDate());
        return ResponseEntity.ok(result);
    }

    /**
     * New endpoint returning a five-day forecast.
     * Supports either city search or direct coordinates.
     *
     * @param city city name
     * @param lat latitude
     * @param lon longitude
     * @return forecast response DTO
     */
    @GetMapping("/forecast")
    public ResponseEntity<WeatherForecastResponseDTO> getForecast(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {

        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(weatherService.getForecastByCity(city));
        }

        if (lat != null && lon != null) {
            return ResponseEntity.ok(weatherService.getForecastByCoordinates(lat, lon));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide either city or lat/lon parameters");
    }
}