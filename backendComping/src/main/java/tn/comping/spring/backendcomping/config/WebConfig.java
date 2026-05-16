package tn.comping.spring.backendcomping.config;

import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path of uploads folder
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        // Configuration générale pour tous les uploads
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // cache images 1 hour

        // Configuration spécifique pour les messages vocaux (chemin plus explicite)
        registry.addResourceHandler("/uploads/voice/**")
                .addResourceLocations("file:./uploads/voice/")
                .setCachePeriod(3600);
    }
}