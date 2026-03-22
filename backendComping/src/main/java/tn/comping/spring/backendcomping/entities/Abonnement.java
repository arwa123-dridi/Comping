package tn.comping.spring.backendcomping.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "abonnements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Abonnement {
    
    @Id
    private String id;
    
    private String suiveurId;       // Qui suit
    private String suiviId;         // Qui est suivi
    
    private Date dateAbonnement;
    
    // Règle métier : pas d'auto-suivi
}

