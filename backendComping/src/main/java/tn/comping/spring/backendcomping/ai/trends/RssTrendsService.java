package tn.comping.spring.backendcomping.ai.trends;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.ai.dto.TrendItem;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse les flux RSS de blogs camping (REI, Outdoor Life, The Dyrt) via Rome.
 */
@Slf4j
@Service
public class RssTrendsService {

    @Value("${ai.rss.feeds}")
    private String rawFeeds;

    private static final int MAX_PER_FEED = 5;

    public List<TrendItem> fetchTrends() {
        List<TrendItem> items = new ArrayList<>();
        if (rawFeeds == null || rawFeeds.isBlank()) return items;

        for (String feedUrl : rawFeeds.split(",")) {
            String url = feedUrl.trim();
            if (url.isEmpty()) continue;
            try {
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(new URL(url).openStream()));
                List<SyndEntry> entries = feed.getEntries();

                int limit = Math.min(MAX_PER_FEED, entries.size());
                for (int i = 0; i < limit; i++) {
                    SyndEntry entry = entries.get(i);
                    String title = entry.getTitle();
                    String desc = entry.getDescription() != null
                            ? entry.getDescription().getValue() : "";
                    // Nettoyer les balises HTML basiques
                    desc = desc.replaceAll("<[^>]+>", "").trim();
                    if (desc.length() > 200) desc = desc.substring(0, 200);

                    if (title != null && !title.isBlank()) {
                        items.add(new TrendItem("rss", title, desc, MAX_PER_FEED - i));
                    }
                }
                log.info("RSS: {} articles récupérés depuis {}", limit, url);
            } catch (Exception e) {
                log.warn("RSS: impossible de parser {} — {}", url, e.getMessage());
            }
        }
        return items;
    }
}
