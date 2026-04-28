package tn.comping.spring.backendcomping.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.comping.spring.backendcomping.dto.ChecklistRequest;
import tn.comping.spring.backendcomping.dto.ChecklistResponse;
import tn.comping.spring.backendcomping.services.serviceImpl.AIChecklistService;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST pour exposer les endpoints liés à l'IA checklist.
 * Angular appellera ces endpoints.
 */
@RestController
@RequestMapping("/api/checklist")
@CrossOrigin(origins = "http://localhost:4200")  // Autorise Angular
@Slf4j
public class ChecklistController {

    @Autowired
    private AIChecklistService aiChecklistService;

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
            return ResponseEntity.status(503).body(response);  // 503 = Service Unavailable
        }
    }
}