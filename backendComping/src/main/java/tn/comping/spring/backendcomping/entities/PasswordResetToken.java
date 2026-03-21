package tn.comping.spring.backendcomping.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {
    
    @Id
    private String id;
    
    private String token;                    // Token unique (UUID)
    
    private String utilisateurEmail;         // Email de l'utilisateur
    
    private LocalDateTime dateCreation;
    
    private LocalDateTime dateExpiration;    // Expiration après 1 heure
    
    private boolean utilise = false;         // Token déjà utilisé ?
    
    private LocalDateTime dateUtilisation;   // Quand le token a été utilisé
}