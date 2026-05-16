package tn.comping.spring.backendcomping.ai.trends;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Récupère les posts populaires de r/camping via l'API publique Reddit (JSON, sans auth).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedditTrendsService {

    private static final String REDDIT_URL =
            "https://www.reddit.com/r/camping/hot.json?limit=15";

    private final WebClient.Builder webClientBuilder;

    public List<TrendItem> fetchTrends() {
        List<TrendItem> items = new ArrayList<>();
        try {
            JsonNode root = webClientBuilder.build()
                    .get()
                    .uri(REDDIT_URL)
                    .header("User-Agent", "CampingApp/1.0")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (root == null) return items;

            JsonNode children = root.path("data").path("children");
            for (JsonNode child : children) {
                JsonNode post = child.path("data");
                String title = post.path("title").asText("");
                String description = post.path("selftext").asText("").trim();
                int score = post.path("ups").asInt(0);

                if (!title.isBlank()) {
                    String excerpt = description.length() > 200
                            ? description.substring(0, 200) : description;
                    items.add(new TrendItem("reddit", title, excerpt, score));
                }
            }
            log.info("Reddit: {} posts récupérés depuis r/camping", items.size());
        } catch (Exception e) {
            log.warn("Reddit: échec de récupération des tendances — {}", e.getMessage());
        }
        return items;
    }
}
