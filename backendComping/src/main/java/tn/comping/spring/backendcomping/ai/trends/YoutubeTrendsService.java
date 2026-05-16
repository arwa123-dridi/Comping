package tn.comping.spring.backendcomping.ai.trends;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Récupère les vidéos camping populaires récentes via YouTube Data API v3.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeTrendsService {

    private static final String YOUTUBE_SEARCH_URL =
            "https://www.googleapis.com/youtube/v3/search";

    @Value("${youtube.api.key}")
    private String apiKey;

    private final WebClient.Builder webClientBuilder;

    public List<TrendItem> fetchTrends() {
        List<TrendItem> items = new ArrayList<>();
        try {
            String publishedAfter = Instant.now()
                    .minus(30, ChronoUnit.DAYS)
                    .toString();

            String uri = UriComponentsBuilder.fromHttpUrl(YOUTUBE_SEARCH_URL)
                    .queryParam("part", "snippet")
                    .queryParam("q", "camping")
                    .queryParam("order", "viewCount")
                    .queryParam("publishedAfter", publishedAfter)
                    .queryParam("maxResults", 10)
                    .queryParam("type", "video")
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();

            JsonNode root = webClientBuilder.build()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null) return items;

            int rank = 10;
            for (JsonNode item : root.path("items")) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText("");
                String desc = snippet.path("description").asText("").trim();
                if (!title.isBlank()) {
                    String excerpt = desc.length() > 200 ? desc.substring(0, 200) : desc;
                    items.add(new TrendItem("youtube", title, excerpt, rank--));
                }
            }
            log.info("YouTube: {} vidéos récupérées sur le camping", items.size());
        } catch (Exception e) {
            log.warn("YouTube: échec de récupération des tendances — {}", e.getMessage());
        }
        return items;
    }
}
