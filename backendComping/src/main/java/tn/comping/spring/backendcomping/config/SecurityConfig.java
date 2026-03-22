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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques
                        .requestMatchers(
                                "/",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/auth/**",
                                "/api/demandes-transport/**",
                                "/api/creneaux-livraison/**",
                                "/api/incidents/**",
                                "/api/conventions-partenaires/**"
                        ).permitAll()

                        // Rôles spécifiques
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/moderateur/**").hasRole("MODERATEUR")
                        .requestMatchers("/api/organisateur/**").hasRole("ORGANISATEUR")

                        // Avis – MODERATEUR ou ADMIN
                        .requestMatchers("/api/avis/statut/**").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/valider").hasAnyRole("MODERATEUR", "ADMIN")
                        .requestMatchers("/api/avis/*/rejeter").hasAnyRole("MODERATEUR", "ADMIN")

                        // Interactions sociales (PHASE 1)
                        .requestMatchers("/api/interactions/**").authenticated()

                        // Tout le reste nécessite authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
