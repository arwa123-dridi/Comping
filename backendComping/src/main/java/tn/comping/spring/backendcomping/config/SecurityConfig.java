package tn.comping.spring.backendcomping.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(List.of("http://localhost:4200"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);

                return config;
            }))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // erreurs Spring
                .requestMatchers("/error").permitAll()

                // AUTH + PUBLIC
                .requestMatchers(
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/ws-chat/**",

                        "/api/sorties/**",
                        "/api/equipes/**",
                        "/api/upload/**",
                        "/api/weather/**",
                        "/api/checklist/**",
                        "/api/demandes-transport/**",
                        "/api/creneaux-livraison/**",
                        "/api/incidents/**",
                        "/api/conventions-partenaires/**",
                        "/api/produits/**",
                        "/api/panier/**",
                        "/api/commandes/**",
                        "/api/recommendations/**",
                        "/api/webhook/**",
                        "/uploads/**"
                ).permitAll()

                // AUTHENTIFIÉ
                .requestMatchers(
                        "/api/planning/**",
                        "/api/recommandations/**",
                        "/api/users/**",
                        "/api/events/**",
                        "/api/posts/**",
                        "/api/chat/**"
                ).authenticated()

                // ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ORGANISATEUR + ADMIN
                .requestMatchers("/api/organisateur/**")
                .hasAnyRole("ORGANISATEUR", "ADMIN")

                // MODERATEUR + ADMIN
                .requestMatchers("/api/moderateur/**")
                .hasAnyRole("MODERATEUR", "ADMIN")

                // Avis modération
                .requestMatchers("/api/avis/statut/**")
                .hasAnyRole("MODERATEUR", "ADMIN")
                .requestMatchers("/api/avis/*/valider")
                .hasAnyRole("MODERATEUR", "ADMIN")
                .requestMatchers("/api/avis/*/rejeter")
                .hasAnyRole("MODERATEUR", "ADMIN")

                // fallback
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}