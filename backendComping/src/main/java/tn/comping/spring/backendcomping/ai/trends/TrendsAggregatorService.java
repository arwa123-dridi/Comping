package tn.comping.spring.backendcomping.ai.trends;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Agrège les tendances camping depuis Reddit, YouTube et RSS en parallèle.
 * Le résultat est mis en cache 6 heures pour éviter de sur-solliciter les sources externes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrendsAggregatorService {

    private final RedditTrendsService redditService;
    private final YoutubeTrendsService youtubeService;
    private final RssTrendsService rssService;

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    /** Fallback utilisé quand toutes les sources externes échouent. */
    private static final List<TrendItem> FALLBACK_TRENDS = List.of(
            new TrendItem("fallback", "Meilleures destinations de camping en France", "Découvrez les plus beaux campings de France pour cet été.", 5),
            new TrendItem("fallback", "Equipement indispensable pour le camping", "Tente, sac de couchage, réchaud... Le guide complet.", 4),
            new TrendItem("fallback", "Camping en famille : conseils et astuces", "Comment organiser un séjour camping mémorable avec les enfants.", 3),
            new TrendItem("fallback", "Recettes de cuisine au feu de camp", "Des plats savoureux à préparer lors de votre prochaine aventure.", 2),
            new TrendItem("fallback", "Camping éco-responsable : nos bonnes pratiques", "Profiter de la nature tout en la respectant.", 1)
    );

    /**
     * Agrège les tendances des 3 sources en parallèle.
     * En cache pendant 6 heures (TTL configuré dans CacheConfig).
     */
    @Cacheable("trends")
    public List<TrendItem> aggregateTrends() {
        log.info("Agrégation des tendances camping depuis Reddit, YouTube, RSS...");

        CompletableFuture<List<TrendItem>> redditFuture =
                CompletableFuture.supplyAsync(redditService::fetchTrends, executor);
        CompletableFuture<List<TrendItem>> youtubeFuture =
                CompletableFuture.supplyAsync(youtubeService::fetchTrends, executor);
        CompletableFuture<List<TrendItem>> rssFuture =
                CompletableFuture.supplyAsync(rssService::fetchTrends, executor);

        List<TrendItem> combined = new ArrayList<>();

        addSafely(combined, redditFuture, "Reddit");
        addSafely(combined, youtubeFuture, "YouTube");
        addSafely(combined, rssFuture, "RSS");

        if (combined.isEmpty()) {
            log.warn("Toutes les sources ont échoué — utilisation du fallback générique");
            return FALLBACK_TRENDS;
        }

        // Dédoublonnage par titre (normalisé) + tri par score décroissant
        return combined.stream()
                .collect(Collectors.toMap(
                        t -> t.title().toLowerCase().trim(),
                        t -> t,
                        (a, b) -> a.score() >= b.score() ? a : b
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(TrendItem::score).reversed())
                .collect(Collectors.toList());
    }

    private void addSafely(List<TrendItem> target,
                           CompletableFuture<List<TrendItem>> future,
                           String sourceName) {
        try {
            List<TrendItem> result = future.get();
            if (result != null) target.addAll(result);
        } catch (Exception e) {
            log.warn("{}: source ignorée suite à une erreur — {}", sourceName, e.getMessage());
        }
    }
}
