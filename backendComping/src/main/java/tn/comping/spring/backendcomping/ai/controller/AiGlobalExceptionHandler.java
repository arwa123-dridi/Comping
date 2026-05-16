package tn.comping.spring.backendcomping.ai.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Gestion d'erreur globale pour les endpoints /api/ai/*.
 * Retourne 503 si Groq est indisponible, 500 sinon.
 */
@Slf4j
@RestControllerAdvice(basePackages = "tn.comping.spring.backendcomping.ai")
public class AiGlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Map<String, String>> handleWebClientError(WebClientResponseException ex) {
        int status = ex.getStatusCode().value();
        log.error("Erreur API externe (status {}): {}", status, ex.getMessage());

        if (status == 401 || status == 403) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Clé API Groq invalide ou manquante. Vérifiez la variable d'environnement GROQ_API_KEY et redémarrez le serveur."));
        }
        if (status == 429) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Quota Groq atteint. Réessayez dans quelques instants."));
        }
        if (status >= 500) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Le service Groq est temporairement indisponible. Réessayez dans quelques instants."));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Erreur Groq API (status " + status + "). Vérifiez votre clé API."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        log.error("Erreur inattendue dans le module IA: {}", ex.getMessage());
        if (ex.getMessage() != null && ex.getMessage().contains("Groq")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Le service Groq est indisponible. Vérifiez votre clé API."));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur interne du module IA."));
    }
}
