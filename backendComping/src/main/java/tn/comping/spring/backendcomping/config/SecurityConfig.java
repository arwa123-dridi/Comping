package tn.comping.spring.backendcomping.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        // 1. Endpoints publics (aucun token requis)
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/upload/**").permitAll()
                        .requestMatchers("/api/weather/**").permitAll()
                        .requestMatchers("/api/checklist/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/demandes-transport/**", "/api/creneaux-livraison/**", "/api/incidents/**",
                                "/api/conventions-partenaires/**", "/api/produits/**", "/uploads/**", "/api/webhook/**").permitAll()

                        // 2. Lectures publiques (GET) sur sorties et équipes
                        .requestMatchers(HttpMethod.GET, "/api/sorties/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/equipes/**").permitAll()

                        // 3. Inscriptions / désinscriptions aux sorties et équipes : utilisateur authentifié (USER, ORGANISATEUR, ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/sorties/**/inscription").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/sorties/**/inscription/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/equipes/**/membres/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/equipes/**/membres/**").authenticated()

                        // 4. Création / modification / suppression de sorties : réservé aux ORGANISATEUR et ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/sorties").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/sorties/**").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/sorties/**").hasAnyRole("ORGANISATEUR", "ADMIN")

                        // 5. Création / modification / suppression d'équipes : réservé aux ORGANISATEUR et ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/equipes").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/equipes/**").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/equipes/**").hasAnyRole("ORGANISATEUR", "ADMIN")

                        // 6. Planning et recommandations : nécessite authentification (tout rôle)
                        .requestMatchers("/api/planning/**").authenticated()
                        .requestMatchers("/api/recommandations/**").authenticated()

                        // 7. Routes pour les rôles spécifiques (admin, moderateur, organisateur)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/moderateur/**").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/organisateur/**").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers("/api/avis/statut/**").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/valider").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/rejeter").hasAnyRole("MODERATEUR", "ADMIN")

                        // 8. Routes utilisateurs et événements : authentifiés
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/events/**").authenticated()
                        .requestMatchers("/api/produits/**").authenticated()

                        // 9. Toute autre requête nécessite une authentification
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