package tn.comping.spring.backendcomping.config;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {



        http.cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("http://localhost:4200"));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/demandes-transport/**",
                                "/api/creneaux-livraison/**",
                                "/api/incidents/**",
                                "/api/conventions-partenaires/**",
                                "/api/reservations/**",
                                "/api/sites/**",
                                "/api/paiements/**",
                                "/api/stripe/**",

                                 "/api/produits/**",
                                  "/uploads/**" 
                        ).permitAll()

                        // Rôles spécifiques
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/moderateur/**").hasRole("MODERATEUR")
                        .requestMatchers("/api/organisateur/**").hasRole("ORGANISATEUR")
                        .requestMatchers("/api/produits/**").authenticated()

                        // Avis – MODERATEUR ou ADMIN
                        .requestMatchers("/api/avis/statut/**").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/valider").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/rejeter").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/events/**").authenticated()
                        // Tout le reste nécessite authentification
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