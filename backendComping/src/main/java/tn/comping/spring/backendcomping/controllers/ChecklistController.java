package tn.comping.spring.backendcomping.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.comping.spring.backendcomping.dto.ChecklistRequest;
import tn.comping.spring.backendcomping.dto.ChecklistResponse;
import tn.comping.spring.backendcomping.dto.WeatherDTO;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;
import tn.comping.spring.backendcomping.services.serviceImpl.WeatherService;

import java.time.LocalDate;

/**
 * Controller REST pour exposer les endpoints liés à l'IA checklist.
 * Angular appellera ces endpoints.
 */
@RestController
@RequestMapping("/api/checklist")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class ChecklistController {

    @Autowired
    private AIChecklistService aiChecklistService;
    @Autowired
    private WeatherService weatherService;

    /**
     * Endpoint pour obtenir la checklist de sécurité recommandée.
     *
     * @param request Les données météo et difficulté
     * @return La checklist recommandée par l'IA
     */
    @PostMapping("/predict")
    public ResponseEntity<ChecklistResponse> predict(@RequestBody ChecklistRequest request) {
        log.info("📥 Requête reçue pour prédiction checklist");
        ChecklistResponse response = aiChecklistService.predictChecklist(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(503).body(response);
        }
    }

    @PostMapping("/recommandation")
    public ResponseEntity<ChecklistResponse> getChecklistByWeather(
            @RequestBody tn.comping.spring.backendcomping.dto.ChecklistRecommandationRequest request) {

        WeatherDTO weather = weatherService.getWeather(request.getCity(), request.getDate());

        ChecklistRequest iaRequest = new ChecklistRequest();
        iaRequest.setTemperature(weather.getTemperature());
        iaRequest.setPrecipitation(weather.getPrecipitation());
        iaRequest.setWind_speed(weather.getWindSpeed());
        iaRequest.setHumidity(weather.getHumidity());
        iaRequest.setDifficulte(request.getDifficulte());

        ChecklistResponse response = aiChecklistService.predictChecklist(iaRequest);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }
}