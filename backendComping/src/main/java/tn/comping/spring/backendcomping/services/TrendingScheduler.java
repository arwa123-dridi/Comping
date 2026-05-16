package tn.comping.spring.backendcomping.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.comping.spring.backendcomping.services.PostService;

/**
 * Recalcule périodiquement les scores de tendance IA
 * pour le module de recommandation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingScheduler {

    private final PostService postService;

    // Toutes les 15 minutes
    @Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 60 * 1000)
    public void recalculateTrendingScores() {
        try {
            log.info("Démarrage recalcul scores IA tendance...");
            postService.recalculateTrendScores();
        } catch (Exception e) {
            log.error("Erreur recalcul scores tendance", e);
        }
    }
}
