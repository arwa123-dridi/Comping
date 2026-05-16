package tn.comping.spring.backendcomping.services.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import tn.comping.spring.backendcomping.dto.ChecklistRequest;
import tn.comping.spring.backendcomping.dto.ChecklistResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service qui communique avec l'API Flask pour obtenir les prédictions IA.
 */
@Service
@Slf4j  // Pour avoir des logs
public class AIChecklistService {

    private final RestTemplate restTemplate;

    @Value("${ia.api.url}")// Récupère l'URL FlastAPI
    private String flastApiUrl;

    @Value("${ai.api.timeout:5000}")
    private int timeout;

    public AIChecklistService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envoie une requête à l'API Flask pour prédire la checklist.
     *
     * @param request Les données météo et difficulté
     * @return La réponse avec la checklist recommandée
     */
    public ChecklistResponse predictChecklist(ChecklistRequest request) {

        log.info("🌤️ Appel à l'IA Flask pour prédiction...");
        log.info("   Température: {}°C", request.getTemperature());
        log.info("   Précipitations: {}mm", request.getPrecipitation());
        log.info("   Vent: {}km/h", request.getWind_speed());
        log.info("   Difficulté: {}/5", request.getDifficulte());

        // Préparer les headers HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Créer l'entité HTTP avec la requête
        HttpEntity<ChecklistRequest> entity = new HttpEntity<>(request, headers);

        try {
            // Appeler l'API Flask
            ResponseEntity<ChecklistResponse> response = restTemplate.postForEntity(
                    flastApiUrl,
                    entity,
                    ChecklistResponse.class
            );

            ChecklistResponse result = response.getBody();

            if (result != null && result.isSuccess()) {
                log.info("✅ Prédiction réussie: {}", result.getChecklistItem());
                log.info("   Confiance: {}%", result.getConfidence() * 100);
            } else if (result != null) {
                log.error("❌ Prédiction échouée: {}", result.getError());
            }

            return result;

        } catch (ResourceAccessException e) {
            // Erreur quand Flask n'est pas accessible
            log.error("❌ Impossible de joindre l'API Flask. Vérifiez que le serveur est lancé sur le port 5000");
            log.error("   Erreur: {}", e.getMessage());

            ChecklistResponse errorResponse = new ChecklistResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError("Service IA indisponible. Vérifiez que Flask tourne sur http://localhost:5000");
            return errorResponse;

        } catch (Exception e) {
            // Autres erreurs
            log.error("❌ Erreur lors de l'appel à l'API IA: {}", e.getMessage());

            ChecklistResponse errorResponse = new ChecklistResponse();
            errorResponse.setSuccess(false);
            errorResponse.setError("Erreur technique: " + e.getMessage());
            return errorResponse;
        }
    }
}