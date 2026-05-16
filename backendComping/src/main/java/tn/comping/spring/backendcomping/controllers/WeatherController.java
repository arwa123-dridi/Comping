package tn.comping.spring.backendcomping.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.dto.WeatherRequest;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @PostMapping
    public ResponseEntity<WeatherDTO> getWeather(@RequestBody WeatherRequest request) {
        System.out.println("hello from here");
        WeatherDTO result = weatherService.getWeather(request.getCity(), request.getDate());
        return ResponseEntity.ok(result);
    }
}