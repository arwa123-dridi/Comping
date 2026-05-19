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
                        // Public routes
                        .requestMatchers(
                                "/",
                                "/error",
                                "/api/auth/**",
                                "/api/upload/**",
                                "/api/weather/**",
                                "/api/checklist/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/webhook/**",
                                "/api/stripe/**",
                                "/ws-chat/**",
                                "/uploads/**"
                        ).permitAll()
                        
                        // GET only public routes
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/produits/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sorties/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sites/**").permitAll()

                        // Role-based access
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/moderateur/**").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/organisateur/**").hasAnyRole("ORGANISATEUR", "ADMIN")
                        .requestMatchers("/api/delivery/**").hasAnyRole("LIVREUR", "ADMIN")

                        // Authenticated only
                        .requestMatchers("/api/planning/**").authenticated()
                        .requestMatchers("/api/recommandations/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/panier/**").authenticated()
                        .requestMatchers("/api/commandes/**").authenticated()
                        .requestMatchers("/api/paiements/**").authenticated()
                        .requestMatchers("/api/incidents/**").authenticated()
                        .requestMatchers("/api/avis/**").authenticated()
                        
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
