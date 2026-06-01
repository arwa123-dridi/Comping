package tn.comping.spring.backendcomping.config;

import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get absolute path of uploads folder (HEAD)
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

        // Global uploads access (HEAD + mariem)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // optional: cache images 1 hour

        // Voice uploads (specific folder) - added by mariem-local
        registry.addResourceHandler("/uploads/voice/**")
                .addResourceLocations("file:./uploads/voice/")
                .setCachePeriod(3600);
    }
}