package tn.comping.spring.backendcomping.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache Caffeine — TTL 6 heures pour le cache "trends" (tendances camping externes).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("trends");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .expireAfterWrite(6, TimeUnit.HOURS)
                        .maximumSize(100)
        );
        return manager;
    }
}
